package com.github.standobyte.jojo.capability.world;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class WorldUtilCap {
    private final World world;
    // FIXME (!) time stop rewrite
    private final WorldTimeStops timeStops = new WorldTimeStops();
    Map<ChunkPos, Integer> timeStopTicks = new HashMap<>();
    Map<ChunkPos, LastToResumeTime> timeResumption = new HashMap<>();
    public boolean gameruleDayLightCycle;
    public boolean gameruleWeatherCycle;
    
    public WorldUtilCap(World world) {
        this.world = world;
    }
    
    public void setTimeStopTicks(int ticks, ChunkPos chunkPos) {
        timeStopTicks.put(chunkPos, Math.max(timeStopTicks.getOrDefault(chunkPos, 0), ticks));
    }
    
    public void setLastToResumeTime(LivingEntity entity, ChunkPos chunkPos, SoundEvent sound, SoundEvent voiceLine) {
        timeResumption.put(chunkPos, new LastToResumeTime(entity, sound, voiceLine));
    }
    
    public void resetTimeStopTicks(ChunkPos chunkPos) {
        timeStopTicks.remove(chunkPos);
        timeResumption.remove(chunkPos);
    }
    
    public boolean isTimeStopped(ChunkPos chunkPos) {
        for (Map.Entry<ChunkPos, Integer> entry : timeStopTicks.entrySet()) {
            if (entry.getValue() > 0 && JojoModConfig.getCommonConfigInstance(world.isClientSide()).inTimeStopRange(entry.getKey(), chunkPos)) {
                return true;
            }
        }
        return false;
    }
    
    public void tickStoppedTime() {
        for (Iterator<Map.Entry<ChunkPos, Integer>> it = timeStopTicks.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<ChunkPos, Integer> entry = it.next();
            if (entry.getValue() > 0) {
                handleTimeResumeSounds(entry);
                entry.setValue(entry.getValue() - 1);
            }
            if (entry.getValue() <= 0) {
                ChunkPos chunkPos = entry.getKey();
                TimeHandler.resumeTime(world, chunkPos, false);
                it.remove();
                timeResumption.remove(chunkPos);
            }
        }
    }
    
    public int getTimeStopTicks(ChunkPos chunkPos) {
        return timeStopTicks.entrySet()
                .stream()
                .filter(center -> JojoModConfig.getCommonConfigInstance(world.isClientSide()).inTimeStopRange(center.getKey(), chunkPos))
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getValue)
                .orElse(0);
    }
    
    private void handleTimeResumeSounds(Map.Entry<ChunkPos, Integer> timeStop) {
        ChunkPos chunkPos = timeStop.getKey();
        LastToResumeTime soundsHandler = timeResumption.get(chunkPos);
        if (soundsHandler != null) {
            soundsHandler.playSounds(timeStop.getValue(), chunkPos, world);
        }
    }
}
