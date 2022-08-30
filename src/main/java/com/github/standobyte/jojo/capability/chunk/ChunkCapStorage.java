package com.github.standobyte.jojo.capability.chunk;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ChunkCapStorage implements IStorage<ChunkCap> {

    @Override
    public INBT writeNBT(Capability<ChunkCap> capability, ChunkCap instance, Direction side) {
        return instance.save();
    }

    @Override
    public void readNBT(Capability<ChunkCap> capability, ChunkCap instance, Direction side, INBT nbt) {
        instance.load((CompoundNBT) nbt);
    }
}
