package com.github.standobyte.jojo.capability.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PlayerUtilCapStorage implements IStorage<PlayerUtilCap> {

    @Override
    public INBT writeNBT(Capability<PlayerUtilCap> capability, PlayerUtilCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<PlayerUtilCap> capability, PlayerUtilCap instance, Direction side, INBT nbt) {
        instance.fromNBT((CompoundNBT) nbt);
    }
}