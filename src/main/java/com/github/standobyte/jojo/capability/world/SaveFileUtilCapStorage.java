package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class SaveFileUtilCapStorage implements IStorage<SaveFileUtilCap> {

    @Override
    public INBT writeNBT(Capability<SaveFileUtilCap> capability, SaveFileUtilCap instance, Direction side) {
        CompoundNBT cnbt = new CompoundNBT();
        cnbt.put("ServerData", instance.save());
        return cnbt;
    }

    @Override
    public void readNBT(Capability<SaveFileUtilCap> capability, SaveFileUtilCap instance, Direction side, INBT nbt) {
        CompoundNBT cnbt = (CompoundNBT) nbt;
        instance.load(cnbt.getCompound("ServerData"));
    }
}