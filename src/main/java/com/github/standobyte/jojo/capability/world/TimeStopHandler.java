package com.github.standobyte.jojo.capability.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket.Phase;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityDataPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.util.mod.ModInteractionUtil;
import com.google.common.collect.HashBiMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionExpiryEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeStopHandler {
    private final World world;
    private final Set<Entity> stoppedInTime = new HashSet<>();
    private final Set<ServerPlayerEntity> playersVisionFrozen = new HashSet<>();
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

            else if (!entity.canUpdate()) {
                tickInStoppedTime(entity);
            }
        }

        if (!timeStopInstances.isEmpty()) {
            Iterator<Map.Entry<Integer, TimeStopInstance>> instanceIter = timeStopInstances.entrySet().iterator();
            while (instanceIter.hasNext()) {
                Map.Entry<Integer, TimeStopInstance> entry = instanceIter.next();
                if (entry.getValue().tick() && !world.isClientSide()) {
                    instanceIter.remove();
                    onRemovedTimeStop(entry.getValue());
                }
            }
            
            if (!playersVisionFrozen.isEmpty()) {
                manualEntitiesDataSync();
            }
        }
    }
    
    private void manualEntitiesDataSync() {
        if (!world.isClientSide()) {
            for (Entity entity : MCUtil.getAllEntities(world)) {
                EntityDataManager entityData = entity.getEntityData();
                if (entityData.isDirty()) {
                    Set<ServerPlayerEntity> trackingPlayers = MCUtil.getTrackingPlayers(entity);
                    List<ServerPlayerEntity> frozenPlayers = new ArrayList<>();
                    
                    List<EntityDataManager.DataEntry<?>> packedData = null;
                    Iterator<ServerPlayerEntity> trackingIterator = trackingPlayers.iterator();
                    while (trackingIterator.hasNext()) {
                        ServerPlayerEntity player = trackingIterator.next();
                        if (playersVisionFrozen.contains(player)) {
                            frozenPlayers.add(player);
                            packedData = entityData.packDirty();
                        }
                    }
                    
                    boolean manualSelectiveSync = packedData != null;
                    if (manualSelectiveSync) {
                        List<EntityDataManager.DataEntry<?>> dataToKeep = packedData;
                        for (ServerPlayerEntity tracking : trackingPlayers) {
                            if (frozenPlayers.contains(tracking)) {
                                tracking.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                                    cap.addDataForTSUnfreeze(entity, dataToKeep);
                                });
                            }
                            else {
                                PacketManager.sendToClient(new TrDirectEntityDataPacket(entity.getId(), packedData), tracking);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void tickInStoppedTime(Entity entity) {
        if (!world.isClientSide()) {
            if (entity instanceof LivingEntity) {
                if (entity.invulnerableTime > 0) {
                    entity.invulnerableTime--;
                }
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.lastHurtByStandTick());
            }
        }
        entity.tickCount--;
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
        MCUtil.getAllEntities(world).forEach(entity -> {
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
        else if (entity instanceof SoulEntity) {
            SoulEntity soulEntity = (SoulEntity) entity;
            if (soulEntity.getOriginEntity() != null) {
                entityToCheck = soulEntity.getOriginEntity();
            }
        }
        
        canMove = canMove || checkEffect && entityToCheck instanceof LivingEntity && ((LivingEntity) entityToCheck).hasEffect(ModStatusEffects.TIME_STOP.get()) || 
                entityToCheck instanceof PlayerEntity && canPlayerMoveInStoppedTime((PlayerEntity) entityToCheck, false)
                || JojoModConfig.getCommonConfigInstance(entity.level.isClientSide()).endermenBeyondTimeSpace.get() && ModInteractionUtil.isEntityEnderman(entityToCheck); // for even more lulz
        
        boolean stopInTime = !canMove;
        entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.updateEntityTimeStop(stopInTime));
        
        if (stopInTime) {
            stoppedInTime.add(entity);
        }
        else {
            stoppedInTime.remove(entity);
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
        MCUtil.getAllEntities(world).forEach(entity -> {
            updateEntityTimeStop(entity, true, true);
        });
    }
    
    private void onRemovedTimeStop(TimeStopInstance instance) {
        MCUtil.getAllEntities(world).forEach(entity -> {
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
        if (isTimeStopped(player.level, player.blockPosition())) {
            canMove = canPlayerMoveInStoppedTime(player, true);
            canSee = canPlayerSeeInStoppedTime(canMove, hasTimeStopAbility(player));
        }
        PacketManager.sendToClient(new TimeStopPlayerStatePacket(canSee, canMove), player);
        
        if (canSee) {
            playersVisionFrozen.remove(player);
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.sendDataOnTSUnfreeze());
        }
        else {
            playersVisionFrozen.add(player);
        }
    }
    
    public Stream<TimeStopInstance> getAllTimeStopInstances() {
        return timeStopInstances.values().stream();
    }
    
    
    
    public static void stopTime(World world, TimeStopInstance instance) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        cap.getTimeStopHandler().addTimeStop(instance);
    }
    
    public static void resumeTime(World world, int instanceId) {
        TimeStopHandler timeStopHandler = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler();
        timeStopHandler.removeTimeStop(timeStopHandler.getById(instanceId));
    }
    
    public static void resumeTime(World world, TimeStopInstance instance) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        cap.getTimeStopHandler().removeTimeStop(instance);
    }
    
    public static TimeStopInstance getTimeStopInstance(World world, int instanceId) {
        TimeStopHandler timeStopHandler = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler();
        return timeStopHandler.getById(instanceId);
    }
    
    public static boolean canPlayerSeeInStoppedTime(PlayerEntity player) {
        return canPlayerSeeInStoppedTime(canPlayerMoveInStoppedTime(player, true), hasTimeStopAbility(player));
    }
    
    public static boolean canPlayerSeeInStoppedTime(boolean canMove, boolean hasTimeStopAbility) {
        return canMove || hasTimeStopAbility;
    }
    
    public static boolean canPlayerMoveInStoppedTime(PlayerEntity player, boolean checkEffect) {
        return checkEffect && player.hasEffect(ModStatusEffects.TIME_STOP.get()) || player.isCreative() || player.isSpectator() || 
                player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).server.isSingleplayerOwner(player.getGameProfile());
    }
    
    public static boolean hasTimeStopAbility(LivingEntity entity) {
        return IStandPower.getStandPowerOptional(entity).map(stand -> 
        stand.hasUnlockedMatching(action -> allowsToSeeInStoppedTime(action, stand, entity)))
                .orElse(false);
    }
    
    private static <P extends IPower<P, ?>> boolean allowsToSeeInStoppedTime(Action<P> action, P power, LivingEntity user) {
        return action.canUserSeeInStoppedTime(user, power) && action.isUnlocked(power);
    }
    
    

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof PlayerEntity)) {
            stopNewEntityInTime(entity, event.getWorld());
        }
    }
    
    public static void stopNewEntityInTime(Entity entity, World world) {
        if (isTimeStopped(world, entity.blockPosition())) {
            world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> 
            cap.getTimeStopHandler().updateEntityTimeStop(entity, false, true));
        }
    }

    
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        sendWorldTimeStopData((ServerPlayerEntity) event.getPlayer());
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        sendWorldTimeStopData((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        sendWorldTimeStopData((ServerPlayerEntity) event.getPlayer());
    }
    
    private static void sendWorldTimeStopData(ServerPlayerEntity player) {
        player.level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            PacketManager.sendToClient(new TimeStopPlayerJoinPacket(Phase.PRE), player);
            cap.getTimeStopHandler().getInstancesInPos(new ChunkPos(player.blockPosition())).forEach(instance -> {
                instance.syncToClient(player);
            });
            
            cap.getTimeStopHandler().sendPlayerState(player);
            
            stopNewEntityInTime(player, player.level);
            PacketManager.sendToClient(new TimeStopPlayerJoinPacket(Phase.POST), player);
        });
    }
    
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if (serverPlayer.getServer().getPlayerList().getPlayerCount() <= 1) {
                serverPlayer.getServer().getAllLevels().forEach(world -> {
                    world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        TimeStopHandler handler = cap.getTimeStopHandler();
                        handler.reset();
                    });
                });
            }
            else {
                player.level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    TimeStopHandler handler = cap.getTimeStopHandler();
                    handler.userStoppedTime(player).ifPresent(instance -> handler.removeTimeStop(instance));
                });
            }
        }
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerWorldTick(WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        event.world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.tick();
        });
        if (event.world.dimension() == World.OVERWORLD) {
            event.world.getCapability(SaveFileUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.tick();
            });
        }
    }



    @SubscribeEvent
    public static void onTSEffectAdded(PotionAddedEvent event) {
        LivingEntity entity = event.getEntityLiving();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getOldPotionEffect() == null && event.getPotionEffect().getEffect() == ModStatusEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler().updateEntityTimeStop(entity, true, false);
            if (!entity.level.isClientSide()) {
                ((ServerWorld) entity.level).getChunkSource().broadcast(entity, (new SPlayEntityEffectPacket(entity.getId(), event.getPotionEffect())));
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, true), entity);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTSEffectExpired(PotionExpiryEvent event) {
        LivingEntity entity = event.getEntityLiving();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getPotionEffect().getEffect() == ModStatusEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            WorldUtilCap worldCap = entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
            worldCap.getTimeStopHandler().updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, false), entity);
                if (worldCap.getTimeStopHandler().getTimeStopTicks(new ChunkPos(entity.blockPosition())) >= 40 && 
                        IStandPower.getStandPowerOptional(entity).map(stand -> 
                        stand.hasPower() && stand.getType() == ModStands.THE_WORLD.getStandType()).orElse(false)) {
                    JojoModUtil.sayVoiceLine(entity, ModSounds.DIO_CANT_MOVE.get());
                };
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTSEffectRemoved(PotionRemoveEvent event) {
        LivingEntity entity = event.getEntityLiving();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getPotion() == ModStatusEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopHandler().updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, false), entity);
            }
        }
    }
    
    
    
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void cancelBlockNeighborUpdate(NeighborNotifyEvent event) {
//        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
//            event.setCanceled(true);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void cancelFluidPlacingBlock(FluidPlaceBlockEvent event) {
//        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
//            event.setNewState(event.getOriginalState());
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void cancelFluidSourceCreation(CreateFluidSourceEvent event) {
//        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
//            event.setResult(Result.DENY);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void cancelCropGrowth(CropGrowEvent.Pre event) {
//        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
//            event.setResult(Result.DENY);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void cancelPistonMovement(PistonEvent.Pre event) {
//        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
//            event.setCanceled(true);
//        }
//    }
//    
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void cancelNoteBlock(NoteBlockEvent.Play event) {
//        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
//            event.setCanceled(true);
//        }
//    }
    
    
    
    public static boolean isTimeStopped(World world, BlockPos blockPos) {
        return isTimeStopped(world, new ChunkPos(blockPos));
    }
    
    public static boolean isTimeStopped(World world, ChunkPos chunkPos) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).map(cap -> cap.getTimeStopHandler().isTimeStopped(chunkPos)).orElse(false);
    }
    
    public static int getTimeStopTicksLeft(World world, ChunkPos chunkPos) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve()
                .flatMap(cap -> cap.getTimeStopHandler().getInstancesInPos(chunkPos).stream()
                        .max((i1, i2) -> i1.getTicksLeft() - i2.getTicksLeft())
                        .map(TimeStopInstance::getTicksLeft))
                .orElse(0);
    }
    

    
    public static ChunkPos getChunkPos(Entity entity) {
        return new ChunkPos(entity.blockPosition());
    }
}
