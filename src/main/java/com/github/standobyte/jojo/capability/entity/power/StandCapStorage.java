package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StandCapStorage implements IStorage<IStandPower> {

    @Override
    public INBT writeNBT(Capability<IStandPower> capability, IStandPower instance, Direction side) {
        return instance.writeNBT();
    }

    @Override
    public void readNBT(Capability<IStandPower> capability, IStandPower instance, Direction side, INBT nbt) {
        instance.readNBT((CompoundNBT) nbt);
    }
}
