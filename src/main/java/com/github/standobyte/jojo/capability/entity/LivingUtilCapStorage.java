package com.github.standobyte.jojo.capability.entity;

import com.github.standobyte.jojo.power.nonstand.type.HamonCharge;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class LivingUtilCapStorage implements IStorage<LivingUtilCap> {

    @Override
    public INBT writeNBT(Capability<LivingUtilCap> capability, LivingUtilCap instance, Direction side) {
        CompoundNBT cnbt = new CompoundNBT();
        if (instance.hamonCharge != null) {
            cnbt.put("HamonCharge", instance.hamonCharge.writeNBT());
        }
        cnbt.putBoolean("UsedTimeStop", instance.hasUsedTimeStopToday);
        return cnbt;
    }

    @Override
    public void readNBT(Capability<LivingUtilCap> capability, LivingUtilCap instance, Direction side, INBT nbt) {
        CompoundNBT cnbt = (CompoundNBT) nbt;
        if (cnbt.contains("HamonCharge", 10)) {
            instance.hamonCharge = new HamonCharge(cnbt.getCompound("HamonCharge"));
        }
        instance.hasUsedTimeStopToday = cnbt.getBoolean("UsedTimeStop");
    }
}