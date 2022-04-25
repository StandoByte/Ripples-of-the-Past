package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class WorldUtilCapStorage implements IStorage<WorldUtilCap> {

    @Override
    public INBT writeNBT(Capability<WorldUtilCap> capability, WorldUtilCap instance, Direction side) {
        CompoundNBT cnbt = new CompoundNBT();
        // FIXME time stop storage?
        cnbt.putBoolean("GameruleDayLightCycle", instance.gameruleDayLightCycle);
        cnbt.putBoolean("GameruleWeatherCycle", instance.gameruleWeatherCycle);
        return cnbt;
    }

    @Override
    public void readNBT(Capability<WorldUtilCap> capability, WorldUtilCap instance, Direction side, INBT nbt) {
        CompoundNBT cnbt = (CompoundNBT) nbt;
        // FIXME time stop storage?
        instance.gameruleDayLightCycle = cnbt.getBoolean("GameruleDayLightCycle");
        instance.gameruleWeatherCycle = cnbt.getBoolean("GameruleWeatherCycle");
    }
}