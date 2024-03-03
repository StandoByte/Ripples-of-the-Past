package com.github.standobyte.jojo.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

@Deprecated
public class MultipleCapsProvider implements ICapabilitySerializable<INBT> {
    private final ICapabilityProvider[] capProviders;
    private final Map<String, ICapabilitySerializable<INBT>> capSerializable;
    
    private MultipleCapsProvider(Builder builder) {
        this.capProviders = builder.capProviders.toArray(new ICapabilityProvider[0]);
        this.capSerializable = builder.capSerializable;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        for (ICapabilityProvider capProvider : capProviders) {
            LazyOptional<T> capability = capProvider.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        capSerializable.forEach((key, cap) -> {
            nbt.put(key, cap.serializeNBT());
        });
        return nbt;
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT cnbt = (CompoundNBT) nbt;
            capSerializable.forEach((key, cap) -> {
                if (cnbt.contains(key)) {
                    cap.deserializeNBT(cnbt.get(key));
                }
            });
        }
    }
    
    

    @Deprecated
    public static class Builder {
        private final List<ICapabilityProvider> capProviders = new ArrayList<>();
        private final Map<String, ICapabilitySerializable<INBT>> capSerializable = new HashMap<>();
        
        public Builder addProvider(ICapabilityProvider capProvider) {
            capProviders.add(capProvider);
            return this;
        }
        
        public Builder addSerializable(ICapabilitySerializable<INBT> capProvider, String nbtKey) {
            capSerializable.put(nbtKey, capProvider);
            return addProvider(capProvider);
        }
        
        public MultipleCapsProvider build() {
            return new MultipleCapsProvider(this);
        }
    }
}
