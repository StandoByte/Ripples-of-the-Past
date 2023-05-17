package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class StandCapProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(IStandPower.class)
    public static Capability<IStandPower> STAND_CAP = null;
    private LazyOptional<IStandPower> instance;
    
    public StandCapProvider(PlayerEntity user) {
        this.instance = LazyOptional.of(() -> new StandPower(user));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return STAND_CAP.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return STAND_CAP.getStorage().writeNBT(STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Stand capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        STAND_CAP.getStorage().readNBT(STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Stand capability LazyOptional is not attached.")), null, nbt);
    }

}
