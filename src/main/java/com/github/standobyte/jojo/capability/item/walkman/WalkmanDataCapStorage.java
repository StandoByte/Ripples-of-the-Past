package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class WalkmanDataCapStorage implements IStorage<WalkmanDataCap> {

    @Override
    public INBT writeNBT(Capability<WalkmanDataCap> capability, WalkmanDataCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<WalkmanDataCap> capability, WalkmanDataCap instance, Direction side, INBT inbt) {
        instance.fromNBT((CompoundNBT) inbt);
    }

}
