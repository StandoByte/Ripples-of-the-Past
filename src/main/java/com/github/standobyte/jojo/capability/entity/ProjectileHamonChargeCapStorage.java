package com.github.standobyte.jojo.capability.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ProjectileHamonChargeCapStorage implements IStorage<ProjectileHamonChargeCap> {

    @Override
    public INBT writeNBT(Capability<ProjectileHamonChargeCap> capability, ProjectileHamonChargeCap instance, Direction side) {
        CompoundNBT cnbt = new CompoundNBT();
        cnbt.putFloat("HamonDamage", instance.hamonBaseDmg);
        cnbt.putInt("ChargeTicks", instance.maxChargeTicks);
        cnbt.putBoolean("Water", instance.water);
        cnbt.putFloat("SpentEnergy", instance.spentEnergy);
        return cnbt;
    }

    @Override
    public void readNBT(Capability<ProjectileHamonChargeCap> capability, ProjectileHamonChargeCap instance, Direction side, INBT nbt) {
        CompoundNBT cnbt = (CompoundNBT) nbt;
        instance.hamonBaseDmg = cnbt.getFloat("HamonDamage");
        instance.maxChargeTicks = cnbt.getInt("ChargeTicks");
        instance.water = cnbt.getBoolean("Water");
        instance.spentEnergy = cnbt.getFloat("SpentEnergy");
    }
}