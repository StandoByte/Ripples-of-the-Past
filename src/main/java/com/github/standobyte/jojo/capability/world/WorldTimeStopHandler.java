package com.github.standobyte.jojo.capability.world;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class WorldTimeStopHandler {
    private final World world;
    private final Set<Entity> stoppedInTime = new HashSet<>();
    private final Set<TimeStopInstance> timeStopInstances = new HashSet<>();
    private boolean gameruleDayLightCycle;
    private boolean gameruleWeatherCycle;
    
    public WorldTimeStopHandler(World world) {
        this.world = world;
    }
    
    public void tick() {
        for (Entity entity : stoppedInTime) {
            if (entity.invulnerableTime > 0 && entity instanceof LivingEntity) {
                entity.invulnerableTime--;
            }
        }
        Iterator<TimeStopInstance> it = timeStopInstances.iterator();
        while (it.hasNext()) {
            TimeStopInstance instance = it.next();
            if (instance.tick()) {
                it.remove();
            }
        }
    }
    
    public void addTimeStop(TimeStopInstance instance) {
        
        
        timeStopInstances.add(instance);
        
        
        if (!world.isClientSide() && timeStopInstances.isEmpty()) {
            ServerWorld serverWorld = (ServerWorld) world;
            gameruleDayLightCycle = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
            world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, serverWorld.getServer());
            gameruleWeatherCycle = world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
            world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, serverWorld.getServer());
        }
    }
    
    public void removeTimeStop(TimeStopInstance instance) {
        timeStopInstances.remove(instance);
        onRemovedTimeStop(instance);
    }
    
    private void onRemovedTimeStop(TimeStopInstance instance) {
        
        
        
        
        
        if (!world.isClientSide() && timeStopInstances.isEmpty()) {
            ServerWorld serverWorld = (ServerWorld) world;
            world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(gameruleDayLightCycle, serverWorld.getServer());
            world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(gameruleWeatherCycle, serverWorld.getServer());
        }
    }
}
