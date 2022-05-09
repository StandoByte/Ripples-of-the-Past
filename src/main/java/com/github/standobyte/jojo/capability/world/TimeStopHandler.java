package com.github.standobyte.jojo.capability.world;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeUtil;
import com.google.common.collect.HashBiMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TimeStopHandler {
    private final World world;
    private final Set<Entity> stoppedInTime = new HashSet<>();
    private final Map<Integer, TimeStopInstance> timeStopInstances = HashBiMap.create();
    private boolean gameruleDayLightCycle;
    private boolean gameruleWeatherCycle;
    
    public TimeStopHandler(World world) {
        this.world = world;
    }
    
    public void tick() {
        Iterator<Entity> entityIter = stoppedInTime.iterator();
        while (entityIter.hasNext()) {
            Entity entity = entityIter.next();
            if (!entity.isAlive()) {
                entityIter.remove();
            }
            
            else if (entity.invulnerableTime > 0 && entity instanceof LivingEntity && !entity.canUpdate()) {
                entity.invulnerableTime--;
            }
        }
        
        Iterator<Map.Entry<Integer, TimeStopInstance>> instanceIter = timeStopInstances.entrySet().iterator();
        while (instanceIter.hasNext()) {
            Map.Entry<Integer, TimeStopInstance> entry = instanceIter.next();
            if (entry.getValue().tick() && !world.isClientSide()) {
                instanceIter.remove();
                onRemovedTimeStop(entry.getValue());
            }
        }
    }
    
    public boolean isTimeStopped(ChunkPos chunkPos) {
        return timeStopInstances.values().stream()
                .anyMatch(instance -> instance.isTimeStopped(chunkPos));
    }
    
    public int getTimeStopTicks(ChunkPos chunkPos) {
        return timeStopInstances.values().stream()
                .filter(instance -> instance.inRange(chunkPos))
                .mapToInt(TimeStopInstance::getTicksLeft)
                .max()
                .orElse(0);
    }
    
    public Set<TimeStopInstance> getInstancesInPos(ChunkPos chunkPos) {
        return timeStopInstances.values().stream()
                .filter(instance -> instance.inRange(chunkPos))
                .collect(Collectors.toSet());
    }
    
    
    
    public void addTimeStop(TimeStopInstance instance) {
        if (timeStopInstances.containsKey(instance.getId())) {
            throw new IllegalStateException("A time stop instance with the id " + instance.getId() + " exists already!");
        }
        if (!userStoppedTime(instance.user).isPresent()) {
            timeStopInstances.put(instance.getId(), instance);
            onAddedTimeStop(instance);
        }
    }
    
    public Optional<TimeStopInstance> userStoppedTime(LivingEntity user) {
        return timeStopInstances.values().stream()
                .filter(instance -> instance.user != null && instance.user.is(user))
                .findFirst();
    }
    
    private void onAddedTimeStop(TimeStopInstance instance) {
        JojoModUtil.getAllEntities(world).forEach(entity -> {
            if (instance.inRange(TimeStopHandler.getChunkPos(entity))) {
                updateEntityTimeStop(entity, false, true);
            }
        });
        
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    instance.syncToClient(player);
                }
            });
            
            if (timeStopInstances.size() == 1) {
                gameruleDayLightCycle = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
                world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, serverWorld.getServer());
                gameruleWeatherCycle = world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
                world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, serverWorld.getServer());
            }
            else {
                timeStopInstances.values().forEach(existingInstance -> existingInstance.removeSoundsIfCrosses(instance));
            }
        }
    }

    public void updateEntityTimeStop(Entity entity, boolean canMove, boolean checkEffect) {
        Entity entityToCheck = entity;
        if (entity instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) entity;
            if (standEntity.getUser() != null) {
                entityToCheck = standEntity.getUser();
            }
        }

        // FIXME (!!!!!!!!!!!!!!!!) wtf is going on with other players' stands
        
        canMove = canMove || checkEffect && entityToCheck instanceof LivingEntity && ((LivingEntity) entityToCheck).hasEffect(ModEffects.TIME_STOP.get()) || 
                entityToCheck instanceof PlayerEntity && TimeUtil.canPlayerMoveInStoppedTime((PlayerEntity) entityToCheck, false)
                || entityToCheck instanceof EndermanEntity; // for the lulz
        
        if (!canMove) {
            stoppedInTime.add(entity);
        }
        
        entity.canUpdate(canMove); 
    }
    
    
    
    public void removeTimeStop(TimeStopInstance instance) {
        if (instance != null) {
            timeStopInstances.remove(instance.getId());
            onRemovedTimeStop(instance);
        }
    }
    
    private void onRemovedTimeStop(TimeStopInstance instance) {
        JojoModUtil.getAllEntities(world).forEach(entity -> {
            ChunkPos pos = TimeStopHandler.getChunkPos(entity);
            if (instance.inRange(pos)) {
                updateEntityTimeStop(entity, !isTimeStopped(pos), true);
            }
        });
        
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    PacketManager.sendToClient(SyncWorldTimeStopPacket.timeResumed(instance.getId()), player);
                }
            });
            if (timeStopInstances.isEmpty()) {
                world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(gameruleDayLightCycle, serverWorld.getServer());
                    world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(gameruleWeatherCycle, serverWorld.getServer());
                });
            }
        }
        
        instance.onRemoved(world);
    }
    
    public TimeStopInstance getById(int id) {
        return timeStopInstances.get(id);
    }
    

    
    public static ChunkPos getChunkPos(Entity entity) {
        return new ChunkPos(entity.blockPosition());
    }
}
