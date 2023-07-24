package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EntityHamonChargeCapStorage implements IStorage<EntityHamonChargeCap> {

    @Override
    public INBT writeNBT(Capability<EntityHamonChargeCap> capability, EntityHamonChargeCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<EntityHamonChargeCap> capability, EntityHamonChargeCap instance, Direction side, INBT nbt) {
        instance.fromNBT((CompoundNBT) nbt);
    }
}