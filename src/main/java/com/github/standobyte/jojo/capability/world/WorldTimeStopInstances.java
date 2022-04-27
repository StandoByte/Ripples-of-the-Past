package com.github.standobyte.jojo.capability.world;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class WorldTimeStopInstances {
    private final World world;
    private final Set<Entity> stoppedInTime = new HashSet<>();
    private final Set<TimeStopInstance> timeStopInstances = new HashSet<>();
    private boolean gameruleDayLightCycle;
    private boolean gameruleWeatherCycle;
    
    public WorldTimeStopInstances(World world) {
        this.world = world;
    }
    
    public void tick() {
        Iterator<Entity> entityIter = stoppedInTime.iterator();
        while (entityIter.hasNext()) {
            Entity entity = entityIter.next();
            if (!entity.isAlive() || entity.canUpdate()) {
                entityIter.remove();
            }
            
            else if (entity.invulnerableTime > 0 && entity instanceof LivingEntity) {
                entity.invulnerableTime--;
            }
        }
        
        Iterator<TimeStopInstance> instanceIter = timeStopInstances.iterator();
        while (instanceIter.hasNext()) {
            TimeStopInstance instance = instanceIter.next();
            if (instance.tick()) {
                instanceIter.remove();
                onRemovedTimeStop(instance);
            }
        }
    }
    
    public boolean isTimeStopped(ChunkPos chunkPos) {
        return timeStopInstances.stream()
                .anyMatch(instance -> instance.isTimeStopped(chunkPos));
    }
    
    public int getTimeStopTicks(ChunkPos chunkPos) {
        return timeStopInstances.stream()
                .filter(instance -> instance.inRange(chunkPos))
                .mapToInt(TimeStopInstance::getTicksLeft)
                .max()
                .orElse(0);
    }
    
    
    
    public void addTimeStop(TimeStopInstance instance) {
        if (!userStoppedTime(instance.user).isPresent()) {
            timeStopInstances.add(instance);
            onAddedTimeStop(instance);
        }
    }
    
    public Optional<TimeStopInstance> userStoppedTime(LivingEntity user) {
        return timeStopInstances
                .stream()
                .filter(instance -> instance.user != null && instance.user.is(user))
                .findFirst();
    }
    
    private void onAddedTimeStop(TimeStopInstance instance) {
        JojoModUtil.getAllEntities(world).forEach(entity -> {
            if (instance.inRange(new ChunkPos(entity.blockPosition()))) {
                updateEntityTimeStop(entity, false, true);
            }
        });
        
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    TimeUtil.sendWorldTimeStopData(player, world, instance.centerPos);
                }
            });
            
            if (timeStopInstances.size() == 1) {
                gameruleDayLightCycle = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
                world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, serverWorld.getServer());
                gameruleWeatherCycle = world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
                world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, serverWorld.getServer());
            }
            else {
                timeStopInstances.forEach(existingInstance -> existingInstance.removeSoundsIfCrosses(instance));
            }
        }
    }

    public void updateEntityTimeStop(Entity entity, boolean canMove, boolean checkEffect) {
        Entity entityToCheck = entity;
//        if (entity instanceof StandEntity) {
//            StandEntity standEntity = (StandEntity) entity;
//            if (standEntity.getUser() != null) {
//                entityToCheck = standEntity.getUser();
//            }
//        }
        
        canMove = canMove || checkEffect && entityToCheck instanceof LivingEntity && ((LivingEntity) entityToCheck).hasEffect(ModEffects.TIME_STOP.get()) || 
                entityToCheck instanceof PlayerEntity && TimeUtil.canPlayerMoveInStoppedTime((PlayerEntity) entityToCheck, false)
                || entityToCheck instanceof EndermanEntity; // for the lulz
        
        if (!canMove) {
            stoppedInTime.add(entity);
        }
        
        entity.canUpdate(canMove); 
    }
    
    
    
    public void removeTimeStop(TimeStopInstance instance) {
        timeStopInstances.remove(instance);
        onRemovedTimeStop(instance);
    }
    
    private void onRemovedTimeStop(TimeStopInstance instance) {
        updateAllEntitiesFreeze();
        
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    PacketManager.sendToClient(SyncWorldTimeStopPacket.timeResumed(instance.centerPos), player);
                }
            });
            if (timeStopInstances.isEmpty()) {
                world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(gameruleDayLightCycle, serverWorld.getServer());
                    world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(gameruleWeatherCycle, serverWorld.getServer());
                });
            }
        }
    }
    
    public void updateAllEntitiesFreeze() {
        JojoModUtil.getAllEntities(world).forEach(entity -> {
            updateEntityTimeStop(entity, true, true);
        });
    }
}
