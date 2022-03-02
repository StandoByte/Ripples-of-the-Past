package com.github.standobyte.jojo.capability.world;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.math.ChunkPos;

public class WorldTimeStops {
    private final Map<ChunkPos, TimeStopInstance> timeStopInstances = new HashMap<>();
    private boolean gameruleDayLightCycle;
    private boolean gameruleWeatherCycle;
    
    public void tick() {
        
    }
}
