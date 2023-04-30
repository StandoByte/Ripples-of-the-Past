package com.github.standobyte.jojo.power.stand;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.power.stand.ModStandActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeStandInstancePacket;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class StandInstance {
    private final StandType<?> standType;
    private final EnumSet<StandPart> parts = EnumSet.allOf(StandPart.class);
    private Optional<ITextComponent> customName = Optional.empty();
    private boolean isDirty;
    
    public StandInstance(@Nonnull StandType<?> standType) {
        this.standType = standType;
    }
    
    public StandType<?> getType() {
        return standType;
    }
    
    public boolean hasPart(StandPart part) {
        return parts.contains(part);
    }
    
    public boolean removePart(StandPart part) {
        boolean removed = parts.remove(part);
        isDirty |= removed;
        return removed;
    }
    
    public boolean addPart(StandPart part) {
        boolean added = parts.add(part);
        isDirty |= added;
        return added;
    }
    
    public EnumSet<StandPart> getAllParts() {
        return parts;
    }
    
    public void setCustomName(ITextComponent customName) {
        isDirty |= this.customName.map(name -> !name.equals(customName)).orElse(customName != null);
        this.customName = Optional.ofNullable(customName);
    }
    
    public ITextComponent getName() {
        return customName.orElse(standType.getName());
    }
    
    public void tick(IStandPower standPower, LivingEntity standUser, World world) {
        if (isDirty) {
            if (!world.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrTypeStandInstancePacket(standUser.getId(), this, -1), standUser);
            }
            isDirty = false;
        }
    }
    
    

    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        nbt.putString("StandType", ModStandActions.STANDS.getKeyAsString(standType));
        
        CompoundNBT missingLimbsNbt = new CompoundNBT();
        boolean limbsMissing = false;
        for (StandPart limbs : StandPart.values()) {
            if (!this.parts.contains(limbs)) {
                missingLimbsNbt.putBoolean(limbs.name(), true);
                limbsMissing = true;
            }
        }
        if (limbsMissing) {
            nbt.put("MissingLimbs", missingLimbsNbt);
        }
        
        customName.ifPresent(name -> nbt.putString("CustomName", ITextComponent.Serializer.toJson(name)));
        
        return nbt;
    }

    public static StandInstance fromNBT(CompoundNBT nbt) {
        String standName = nbt.getString("StandType");
        StandType<?> standType = ModStandActions.STANDS.getRegistry().getValue(new ResourceLocation(standName));
        if (standType == null) {
            return null;
        }
        
        StandInstance instance = new StandInstance(standType);
        
        if (nbt.contains("MissingLimbs", MCUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT missingLimbsNbt = nbt.getCompound("MissingLimbs");
            for (StandPart limbs : StandPart.values()) {
                if (missingLimbsNbt.getBoolean(limbs.name())) {
                    instance.parts.remove(limbs);
                }
            }
        }

        if (nbt.contains("CustomName", MCUtil.getNbtId(StringNBT.class))) {
            String name = nbt.getString("CustomName");
            try {
                instance.setCustomName(ITextComponent.Serializer.fromJson(name));
            } catch (Exception exception) {
                JojoMod.getLogger().warn("Failed to parse custom Stand name {}", name, exception);
            }
        }
        
        return instance;
    }
    
    public void toBuf(PacketBuffer buf) {
        buf.writeRegistryId(standType);
        
        Set<StandPart> missingParts = EnumSet.complementOf(parts);
        buf.writeVarInt(missingParts.size());
        missingParts.forEach(part -> buf.writeEnum(part));
        
        DataSerializers.OPTIONAL_COMPONENT.write(buf, customName);
    }
    
    public static StandInstance fromBuf(PacketBuffer buf) {
        StandType<?> standType = buf.readRegistryIdSafe(StandType.class);
        StandInstance standInstance = new StandInstance(standType);
        
        int missingPartsCount = buf.readVarInt();
        for (int i = 0; i < missingPartsCount; i++) {
            standInstance.parts.remove(buf.readEnum(StandPart.class));
        }
        
        standInstance.customName = DataSerializers.OPTIONAL_COMPONENT.read(buf);
        
        return standInstance;
    }
    
    
    
    
    public enum StandPart {
        MAIN_BODY,
        ARMS,
        LEGS
    }

}
