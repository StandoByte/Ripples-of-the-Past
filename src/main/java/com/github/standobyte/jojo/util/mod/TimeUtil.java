package com.github.standobyte.jojo.util.mod;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCap;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket.Phase;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

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
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
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
            
            TimeUtil.stopNewEntityInTime(player, player.level);
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
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).map(cap -> cap.getTimeStopHandler().isTimeStopped(chunkPos)).orElse(false);
    }
    
    public static int getTimeStopTicksLeft(World world, ChunkPos chunkPos) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve()
                .flatMap(cap -> cap.getTimeStopHandler().getInstancesInPos(chunkPos).stream()
                        .max((i1, i2) -> i1.getTicksLeft() - i2.getTicksLeft())
                        .map(TimeStopInstance::getTicksLeft))
                .orElse(0);
    }
}
