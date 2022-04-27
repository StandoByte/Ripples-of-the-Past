package com.github.standobyte.jojo.capability.world;

import net.minecraft.world.World;

public class WorldUtilCap {
    private final World world;
    final WorldTimeStopInstances timeStops;
    
    public WorldUtilCap(World world) {
        this.world = world;
        this.timeStops = new WorldTimeStopInstances(world);
    }
    
    public WorldTimeStopInstances getWorldTimeStops() {
        return timeStops;
    }
}
