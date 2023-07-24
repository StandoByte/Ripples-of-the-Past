package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EntityHamonChargeCapProvider implements ICapabilitySerializable<INBT>{
    @CapabilityInject(EntityHamonChargeCap.class)
    public static Capability<EntityHamonChargeCap> CAPABILITY = null;
    private LazyOptional<EntityHamonChargeCap> instance;
    
    public EntityHamonChargeCapProvider(Entity entity) {
        this.instance = LazyOptional.of(() -> new EntityHamonChargeCap(entity));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Hamon charge capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Hamon charge capability LazyOptional is not attached.")), null, nbt);
    }
}
