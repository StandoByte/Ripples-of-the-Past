package com.github.standobyte.jojo.itemtracking.itemcap;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TrackerItemStackStorage implements IStorage<TrackerItemStack> {

    @Override
    public INBT writeNBT(Capability<TrackerItemStack> capability, TrackerItemStack instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<TrackerItemStack> capability, TrackerItemStack instance, Direction side, INBT nbt) {
        instance.fromNBT(nbt);
    }
}