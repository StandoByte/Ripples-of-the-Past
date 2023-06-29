package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class WalkmanCassetteSlotStorage implements IStorage<WalkmanCassetteSlotCap> {

    @Override
    public INBT writeNBT(Capability<WalkmanCassetteSlotCap> capability, WalkmanCassetteSlotCap instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<WalkmanCassetteSlotCap> capability, WalkmanCassetteSlotCap instance, Direction side, INBT nbt) {
        instance.deserializeNBT((CompoundNBT) nbt);
    }
}