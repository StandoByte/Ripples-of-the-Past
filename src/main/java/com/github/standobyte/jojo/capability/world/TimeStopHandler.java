package com.github.standobyte.jojo.capability.world;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerStatePacket;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;
import com.google.common.collect.HashBiMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TimeStopHandler {
    private final World world;
    private final Set<Entity> stoppedInTime = new HashSet<>();
    private final Map<Integer, TimeStopInstance> timeStopInstances = HashBiMap.create();
    
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
            
            else if (entity instanceof LivingEntity && !entity.canUpdate()) {
            	if (entity.invulnerableTime > 0) {
            		entity.invulnerableTime--;
            	}
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.lastHurtByStandTick());
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
        if (!timeStopInstances.containsKey(instance.getId()) && !userStoppedTime(instance.user).isPresent()) {
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
                    sendPlayerState(player);
                }
            });
            
            if (timeStopInstances.size() == 1) {
            	SaveFileUtilCapProvider.getSaveFileCap(serverWorld.getServer()).setTimeStopGamerules(serverWorld);
            }
            else {
                timeStopInstances.values().forEach(existingInstance -> existingInstance.removeSoundsIfCrosses(instance));
            }
        }
    }
    
    boolean hasTimeStopInstances() {
    	return !timeStopInstances.isEmpty();
    }

    public void updateEntityTimeStop(Entity entity, boolean canMove, boolean checkEffect) {
        Entity entityToCheck = entity;
        if (entity instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) entity;
            if (standEntity.getUser() != null) {
                entityToCheck = standEntity.getUser();
            }
        }
        
        canMove = canMove || checkEffect && entityToCheck instanceof LivingEntity && ((LivingEntity) entityToCheck).hasEffect(ModEffects.TIME_STOP.get()) || 
                entityToCheck instanceof PlayerEntity && TimeUtil.canPlayerMoveInStoppedTime((PlayerEntity) entityToCheck, false)
                || entityToCheck instanceof EndermanEntity; // for the lulz
        
        boolean stopInTime = !canMove;
        entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.updateEntityTimeStop(stopInTime));
        
        if (stopInTime) {
            stoppedInTime.add(entity);
        }
    }
    
    
    
    public void removeTimeStop(TimeStopInstance instance) {
        if (instance != null) {
            timeStopInstances.remove(instance.getId());
            onRemovedTimeStop(instance);
        }
    }
    
    public void reset() {
    	timeStopInstances.clear();
        JojoModUtil.getAllEntities(world).forEach(entity -> {
            updateEntityTimeStop(entity, true, true);
        });
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
                    PacketManager.sendToClient(TimeStopInstancePacket.timeResumed(instance.getId()), player);
                    sendPlayerState(player);
                }
            });
            if (timeStopInstances.isEmpty()) {
            	SaveFileUtilCapProvider.getSaveFileCap(serverWorld.getServer()).restoreTimeStopGamerules(serverWorld);
            }
        }
        
        instance.onRemoved(world);
    }
    
    public TimeStopInstance getById(int id) {
        return timeStopInstances.get(id);
    }
    
    public void sendPlayerState(ServerPlayerEntity player) {
    	boolean canMove = true;
    	boolean canSee = true;
    	if (TimeUtil.isTimeStopped(player.level, player.blockPosition())) {
        	canMove = TimeUtil.canPlayerMoveInStoppedTime(player, true);
        	canSee = TimeUtil.canPlayerSeeInStoppedTime(canMove, TimeUtil.hasTimeStopAbility(player));
    	}
        PacketManager.sendToClient(new TimeStopPlayerStatePacket(canSee, canMove), player);
    }
    
    public Collection<TimeStopInstance> getAllTimeStopInstances() {
    	return Collections.unmodifiableCollection(timeStopInstances.values());
    }
    

    
    public static ChunkPos getChunkPos(Entity entity) {
        return new ChunkPos(entity.blockPosition());
    }
}
