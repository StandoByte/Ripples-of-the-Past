package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class NonStandCapProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(INonStandPower.class)
    public static Capability<INonStandPower> NON_STAND_CAP = null;
    private LazyOptional<INonStandPower> instance;
    
    public NonStandCapProvider(PlayerEntity user) {
        this.instance = LazyOptional.of(() -> new NonStandPower(user));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return NON_STAND_CAP.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return NON_STAND_CAP.getStorage().writeNBT(NON_STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Non-stand power capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        NON_STAND_CAP.getStorage().readNBT(NON_STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Non-stand power capability LazyOptional is not attached.")), null, nbt);
    }

}
