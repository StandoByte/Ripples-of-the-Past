package com.github.standobyte.jojo.capability.world;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.world.World;

public class WorldUtilCap {
    private final World world;
    final TimeStopHandler timeStops;
    private final Queue<EggEntity> chargedEggs = new LinkedList<>();
    
    public WorldUtilCap(World world) {
        this.world = world;
        this.timeStops = new TimeStopHandler(world);
    }
    
    public void tick() {
        timeStops.tick();
        if (!world.isClientSide()) {
            tickEggsQueue();
        }
    }
    
    
    public TimeStopHandler getTimeStopHandler() {
        return timeStops;
    }
    
    
    public void addChargedEggEntity(EggEntity entity) {
        chargedEggs.add(entity);
    }
    
    public Optional<EggEntity> eggChargingChicken(Entity chicken) {
        if (chargedEggs.isEmpty()) {
            return Optional.empty();
        }
        
        return chargedEggs.stream()
                .filter(egg -> egg.getBoundingBox().intersects(chicken.getBoundingBox()))
                .findFirst();
    }
    
    private void tickEggsQueue() {
        Iterator<EggEntity> it = chargedEggs.iterator();
        while (it.hasNext()) {
            EggEntity entity = it.next();
            if (!entity.isAlive()) {
                it.remove();
            }
        }
    }
}
