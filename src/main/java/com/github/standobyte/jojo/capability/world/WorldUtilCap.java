package com.github.standobyte.jojo.capability.world;

import net.minecraft.world.World;

public class WorldUtilCap {
    private final World world;
    final TimeStopHandler timeStops;
    
    public WorldUtilCap(World world) {
        this.world = world;
        this.timeStops = new TimeStopHandler(world);
    }
    
    public void tick() {
        timeStops.tick();
    }
    
    public TimeStopHandler getTimeStopHandler() {
        return timeStops;
    }
}
