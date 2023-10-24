package com.github.standobyte.jojo.power.impl.stand;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeStandInstancePacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
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
    private Optional<ResourceLocation> standSkin = Optional.empty();
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
    
    public void setCustomSkin(Optional<ResourceLocation> skinLocation, @Nullable IStandPower power) {
        if (skinLocation.isPresent() && skinLocation.get().equals(standType.getRegistryName())) {
            skinLocation = Optional.empty();
        }
        
        isDirty |= (this.standSkin.isPresent() ^ skinLocation.isPresent())
                || this.standSkin.isPresent() && skinLocation.map(skinNew -> !skinNew.equals(this.standSkin.get())).orElse(false);
        this.standSkin = skinLocation;
        if (power != null) {
            standType.onStandSkinSet(power, skinLocation);
        }
    }
    
    public Optional<ResourceLocation> getSelectedSkin() {
        return standSkin;
    }
    
    public void tick(IStandPower standPower, LivingEntity standUser, World world) {
        syncIfDirty(standUser);
    }
    
    public void syncIfDirty(LivingEntity standUser) {
        if (isDirty && !standUser.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrTypeStandInstancePacket(standUser.getId(), this, -1), standUser);
        }
        isDirty = false;
    }
    
    

    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        nbt.putString("StandType", JojoCustomRegistries.STANDS.getKeyAsString(standType));
        
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
        standSkin.ifPresent(skinId -> nbt.putString("Skin", skinId.toString()));
        
        return nbt;
    }

    public static StandInstance fromNBT(CompoundNBT nbt) {
        String standName = nbt.getString("StandType");
        StandType<?> standType = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standName));
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
        
        instance.setCustomSkin(MCUtil.getNbtElement(nbt, "Skin", StringNBT.class)
                .map(StringNBT::getAsString).map(ResourceLocation::new), 
                null);
        
        return instance;
    }
    
    public void toBuf(PacketBuffer buf) {
        buf.writeRegistryId(standType);
        
        Set<StandPart> missingParts = EnumSet.complementOf(parts);
        buf.writeVarInt(missingParts.size());
        missingParts.forEach(part -> buf.writeEnum(part));
        
        DataSerializers.OPTIONAL_COMPONENT.write(buf, customName);
        NetworkUtil.writeOptional(buf, standSkin, skinResLoc -> buf.writeResourceLocation(skinResLoc));
    }
    
    public static StandInstance fromBuf(PacketBuffer buf) {
        StandType<?> standType = buf.readRegistryIdSafe(StandType.class);
        StandInstance standInstance = new StandInstance(standType);
        
        int missingPartsCount = buf.readVarInt();
        for (int i = 0; i < missingPartsCount; i++) {
            standInstance.parts.remove(buf.readEnum(StandPart.class));
        }
        
        standInstance.customName = DataSerializers.OPTIONAL_COMPONENT.read(buf);
        standInstance.standSkin = NetworkUtil.readOptional(buf, () -> buf.readResourceLocation());
        
        return standInstance;
    }
    
    
    
    
    public enum StandPart {
        MAIN_BODY,
        ARMS,
        LEGS
    }

}
