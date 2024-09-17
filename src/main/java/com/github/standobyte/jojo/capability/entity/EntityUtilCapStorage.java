package com.github.standobyte.jojo.capability.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EntityUtilCapStorage implements IStorage<EntityUtilCap> {

    @Override
    public INBT writeNBT(Capability<EntityUtilCap> capability, EntityUtilCap instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<EntityUtilCap> capability, EntityUtilCap instance, Direction side, INBT nbt) {
        instance.deserializeNBT((CompoundNBT) nbt);
    }
}