package com.github.standobyte.jojo.capability.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MerchantDataProvider implements ICapabilitySerializable<INBT>{
    @CapabilityInject(MerchantData.class)
    public static Capability<MerchantData> CAPABILITY = null;
    private LazyOptional<MerchantData> instance;
    
    public MerchantDataProvider(LivingEntity entity, IMerchant asMerchant) {
        this.instance = LazyOptional.of(() -> new MerchantData(entity, asMerchant));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Merchant data LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Merchant data LazyOptional is not attached.")), null, nbt);
    }
}
