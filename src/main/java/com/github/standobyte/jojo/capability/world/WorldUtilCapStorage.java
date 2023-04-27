package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class WorldUtilCapStorage implements IStorage<WorldUtilCap> {

    @Override
    public INBT writeNBT(Capability<WorldUtilCap> capability, WorldUtilCap instance, Direction side) {
        CompoundNBT nbt = new CompoundNBT();
        return nbt;
    }

    @Override
    public void readNBT(Capability<WorldUtilCap> capability, WorldUtilCap instance, Direction side, INBT nbt) {
    }
}