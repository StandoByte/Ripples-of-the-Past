package com.github.standobyte.jojo.power.stand;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class StandInstance {
    private final StandType<?> standType;
    private final Set<StandParts> limbs = EnumSet.allOf(StandParts.class);
    private Optional<ITextComponent> customName = Optional.empty();
    private final ResolveCounter resolveCounter;
    
    public StandInstance(@Nonnull StandType<?> standType, IStandPower standPower) {
        this.standType = standType;
        this.resolveCounter = new ResolveCounter(standPower);
    }
    
    public boolean removeLimbs(StandParts limbs) {
        return this.limbs.remove(limbs);
    }
    
    public boolean returnLimbs(StandParts limbs) {
        return this.limbs.add(limbs);
    }
    
    public void setCustomName(ITextComponent customName) {
        this.customName = Optional.ofNullable(customName);
    }
    
    public Optional<ITextComponent> getCustomName() {
        return customName;
    }
    
    public ResolveCounter getResolveCounter() {
        return resolveCounter;
    }
    
    

    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        nbt.putString("StandType", ModStandTypes.Registry.getKeyAsString(standType));
        
        CompoundNBT missingLimbsNbt = new CompoundNBT();
        boolean limbsMissing = false;
        for (StandParts limbs : StandParts.values()) {
            if (!this.limbs.contains(limbs)) {
                missingLimbsNbt.putBoolean(limbs.name(), true);
                limbsMissing = true;
            }
        }
        if (limbsMissing) {
            nbt.put("MissingLimbs", missingLimbsNbt);
        }
        
        customName.ifPresent(name -> nbt.putString("CustomName", ITextComponent.Serializer.toJson(name)));
        
        nbt.put("Resolve", resolveCounter.writeNBT());
        
        return nbt;
    }

    public static StandInstance fromNBT(CompoundNBT nbt, IStandPower standPower) {
        String standName = nbt.getString("StandType");
        StandType<?> standType = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standName));
        if (standType == null) {
            throw new IllegalStateException("Invalid Stand type name read from NBT!");
        }
        
        StandInstance instance = new StandInstance(standType, standPower);
        
        if (nbt.contains("MissingLimbs", JojoModUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT missingLimbsNbt = nbt.getCompound("MissingLimbs");
            for (StandParts limbs : StandParts.values()) {
                if (missingLimbsNbt.getBoolean(limbs.name())) {
                    instance.limbs.remove(limbs);
                }
            }
        }

        if (nbt.contains("CustomName", JojoModUtil.getNbtId(StringNBT.class))) {
            String name = nbt.getString("CustomName");
            try {
                instance.setCustomName(ITextComponent.Serializer.fromJson(name));
            } catch (Exception exception) {
                JojoMod.getLogger().warn("Failed to parse custom Stand name {}", name, exception);
            }
        }
        
        if (nbt.contains("Resolve", JojoModUtil.getNbtId(CompoundNBT.class))) {
            instance.resolveCounter.readNbt(nbt.getCompound("Resolve"));
        }
        
        return instance;
    }
    
    
    
    
    public enum StandParts {
        ARMS,
        LEGS
    }

}
