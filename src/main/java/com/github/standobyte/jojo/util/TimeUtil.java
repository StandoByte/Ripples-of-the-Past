package com.github.standobyte.jojo.util;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCap;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.google.common.collect.Streams;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.world.BlockEvent.CreateFluidSourceEvent;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.event.world.BlockEvent.FluidPlaceBlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeUtil {

    public static void stopTime(World world, TimeStopInstance instance) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        cap.getWorldTimeStops().addTimeStop(instance);
    }
    
    public static void resumeTime(World world, TimeStopInstance instance) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        cap.getWorldTimeStops().removeTimeStop(instance);
    }
    
    public static boolean canPlayerSeeInStoppedTime(PlayerEntity player) {
        return canPlayerSeeInStoppedTime(canPlayerMoveInStoppedTime(player, true), hasTimeStopAbility(player));
    }
    
    private static boolean canPlayerSeeInStoppedTime(boolean canMove, boolean hasTimeStopAbility) {
        return canMove || hasTimeStopAbility;
    }
    
    public static boolean canPlayerMoveInStoppedTime(PlayerEntity player, boolean checkEffect) {
        return checkEffect && player.hasEffect(ModEffects.TIME_STOP.get()) || player.isCreative() || player.isSpectator() || 
                player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).server.isSingleplayerOwner(player.getGameProfile());
    }
    
    private static boolean hasTimeStopAbility(LivingEntity entity) {
        return 
                IStandPower.getStandPowerOptional(entity).map(stand -> {
                    return Streams.concat(stand.getAttacks().stream(), stand.getAbilities().stream())
                            .anyMatch(action -> allowsToSeeInStoppedTime(action, stand, entity));
                }).orElse(false) 
                ||
                INonStandPower.getNonStandPowerOptional(entity).map(power -> {
                    return Streams.concat(power.getAttacks().stream(), power.getAbilities().stream())
                            .anyMatch(action -> allowsToSeeInStoppedTime(action, power, entity));
                }).orElse(false);
    }
    
    private static <P extends IPower<P, ?>> boolean allowsToSeeInStoppedTime(Action<P> action, P power, LivingEntity user) {
        return action.canUserSeeInStoppedTime(user, power) && action.isUnlocked(power);
    }
    
    

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (isTimeStopped(event.getWorld(), entity.blockPosition())) {
            event.getWorld().getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> 
            cap.getWorldTimeStops().updateEntityTimeStop(entity, false, true));
        }
    }

    
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        sendWorldTimeStopData(player, player.level, new ChunkPos(player.blockPosition()));
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        sendWorldTimeStopData(player, player.server.getLevel(event.getTo()), new ChunkPos(player.blockPosition()));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        sendWorldTimeStopData(player, player.level, new ChunkPos(player.blockPosition()));
    }
    
    public static void sendWorldTimeStopData(ServerPlayerEntity player, World world, ChunkPos chunkPos) {
        if (isTimeStopped(world, player.blockPosition())) {
            boolean canMove = canPlayerMoveInStoppedTime(player, true);
            boolean canSee = canPlayerSeeInStoppedTime(canMove, hasTimeStopAbility(player));
            PacketManager.sendToClient(new SyncWorldTimeStopPacket(
                    world.getCapability(WorldUtilCapProvider.CAPABILITY).map(cap -> cap.getWorldTimeStops().getTimeStopTicks(chunkPos)).orElse(0), 
                    chunkPos, canSee, canMove), player);
        }
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerWorldTick(WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        event.world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.getWorldTimeStops().tick();
        });
    }



    @SubscribeEvent
    public static void onTSEffectAdded(PotionAddedEvent event) {
        LivingEntity entity = event.getEntityLiving();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getOldPotionEffect() == null && event.getPotionEffect().getEffect() == ModEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getWorldTimeStops().updateEntityTimeStop(entity, false, false);
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
        if (event.getPotionEffect().getEffect() == ModEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            WorldUtilCap worldCap = entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
            worldCap.getWorldTimeStops().updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, false), entity);
                if (worldCap.getWorldTimeStops().getTimeStopTicks(new ChunkPos(entity.blockPosition())) >= 40 && 
                        IStandPower.getStandPowerOptional(entity).map(stand -> stand.getType() == ModStandTypes.THE_WORLD.get()).orElse(false)) {
                    JojoModUtil.sayVoiceLine(entity, ModSounds.DIO_CANT_MOVE.get());
                };
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTSEffectRemoved(PotionRemoveEvent event) {
        LivingEntity entity = event.getEntityLiving();
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        if (event.getPotion() == ModEffects.TIME_STOP.get() && isTimeStopped(entity.level, chunkPos)) {
            entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getWorldTimeStops().updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), chunkPos, false), entity);
            }
        }
    }
    
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelBlockNeighborUpdate(NeighborNotifyEvent event) {
        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelFluidSpread(FluidPlaceBlockEvent event) {
        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
            event.setNewState(event.getOriginalState());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelFluidSourceCreation(CreateFluidSourceEvent event) {
        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
            event.setResult(Result.DENY);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelCropGrowth(CropGrowEvent.Pre event) {
        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
            event.setResult(Result.DENY);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelPistonMovement(PistonEvent.Pre event) {
        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelNoteBlock(NoteBlockEvent.Play event) {
        if (isTimeStopped((World) event.getWorld(), event.getPos())) {
            event.setCanceled(true);
        }
    }
    
    
    
    public static boolean isTimeStopped(World world, BlockPos blockPos) {
        return isTimeStopped(world, new ChunkPos(blockPos));
    }  
    
    public static boolean isTimeStopped(World world, ChunkPos chunkPos) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).map(cap -> cap.getWorldTimeStops().isTimeStopped(chunkPos)).orElse(false);
    }
}
