package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ProjectileHamonChargeCapStorage implements IStorage<ProjectileHamonChargeCap> {

    @Override
    public INBT writeNBT(Capability<ProjectileHamonChargeCap> capability, ProjectileHamonChargeCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<ProjectileHamonChargeCap> capability, ProjectileHamonChargeCap instance, Direction side, INBT nbt) {
        instance.fromNBT((CompoundNBT) nbt);
    }
}