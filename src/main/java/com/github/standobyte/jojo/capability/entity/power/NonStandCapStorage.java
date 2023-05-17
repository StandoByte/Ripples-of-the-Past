package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class NonStandCapStorage implements IStorage<INonStandPower> {

    @Override
    public INBT writeNBT(Capability<INonStandPower> capability, INonStandPower instance, Direction side) {
        return instance.writeNBT();
    }

    @Override
    public void readNBT(Capability<INonStandPower> capability, INonStandPower instance, Direction side, INBT nbt) {
        instance.readNBT((CompoundNBT) nbt);
    }
}
