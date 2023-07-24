package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ProjectileHamonChargeCapProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(ProjectileHamonChargeCap.class)
    public static Capability<ProjectileHamonChargeCap> CAPABILITY = null;
    private LazyOptional<ProjectileHamonChargeCap> instance;
    
    public ProjectileHamonChargeCapProvider(Entity projectile) {
        this.instance = LazyOptional.of(() -> new ProjectileHamonChargeCap(projectile));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Projectile capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Projectile capability LazyOptional is not attached.")), null, nbt);
    }

}