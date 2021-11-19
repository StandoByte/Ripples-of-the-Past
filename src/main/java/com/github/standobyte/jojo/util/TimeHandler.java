package com.github.standobyte.jojo.util;

import java.util.Set;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.TimeStop;
import com.github.standobyte.jojo.capability.world.WorldUtilCap;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.VampirismPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
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
public class TimeHandler {
    private static final ImmutableSet.Builder<Action> TIME_STOP_ACTIONS_BUILDER = ImmutableSet.builder(); 
    private static Set<Action> ALLOW_MOVING_IN_STOPPED_TIME = null;
    
    public static void setTimeResumeSounds(World world, int timeStopTicks, TimeStop timeStop, LivingEntity timeStopUser) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        if (timeStopTicks > cap.getTimeStopTicks()) {
            cap.setLastToResumeTime(timeStopUser, timeStop.getTimeResumeSfx(), timeStop.getTimeResumeVoiceLine());
        }
    }

    public static boolean stopTime(World world, int ticks) {
        WorldUtilCap cap = world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get();
        if (cap.setTimeStopTicks(ticks)) {
            JojoModUtil.getAllEntities(world).forEach(entity -> {
                updateEntityTimeStop(entity, false, true);
            });
            
            if (!world.isClientSide()) {
                ServerWorld serverWorld = (ServerWorld) world;
                serverWorld.players().forEach(player -> {
                    if (player.level == world) {
                        sendWorldTimeStopData(player, world);
                    }
                });
                cap.gameruleDayLightCycle = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
                world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, serverWorld.getServer());
                cap.gameruleWeatherCycle = world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
                world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, serverWorld.getServer());
            }
            return true;
        }
        return false;
    }
    
    public static void resumeTime(World world, boolean resetCap) {
        JojoModUtil.getAllEntities(world).forEach(entity -> {
            updateEntityTimeStop(entity, true, true);
        });
        
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    PacketManager.sendToClient(SyncWorldTimeStopPacket.timeResumed(), player);
                }
            });
            world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(cap.gameruleDayLightCycle, serverWorld.getServer());
                world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(cap.gameruleWeatherCycle, serverWorld.getServer());
            });
        }
        if (resetCap) {
            world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.resetTimeStopTicks();
            });
        }
    }
    
    
    
    public static boolean canPlayerSeeInStoppedTime(PlayerEntity player) {
        return canPlayerSeeInStoppedTime(canPlayerMoveInStoppedTime(player, true), hasTimeStopAbility(player));
    }
    
    private static boolean canPlayerSeeInStoppedTime(boolean canMove, boolean hasTimeStopAbility) {
        return canMove || hasTimeStopAbility;
    }
    
    private static boolean canPlayerMoveInStoppedTime(PlayerEntity player, boolean checkEffect) {
        return checkEffect && player.hasEffect(ModEffects.TIME_STOP.get()) || player.isCreative() || player.isSpectator() || 
                player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).server.isSingleplayerOwner(player.getGameProfile());
    }
    
    private static boolean hasTimeStopAbility(LivingEntity entity) {
        return IStandPower.getStandPowerOptional(entity).map(stand -> {
            for (Action ability : stand.getAbilities()) {
                if (stand.isActionUnlocked(ability) && ALLOW_MOVING_IN_STOPPED_TIME.contains(ability)) {
                    return true;
                }
            }
            for (Action attack : stand.getAttacks()) {
                if (stand.isActionUnlocked(attack) && ALLOW_MOVING_IN_STOPPED_TIME.contains(attack)) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }
    
    

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (isTimeStopped(event.getWorld())) {
            updateEntityTimeStop(event.getEntity(), false, true);
        }
    }
    
    public static void updateEntityTimeStop(Entity entity, boolean canMove, boolean checkEffect) {
        entity.canUpdate(canMove || checkEffect && entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(ModEffects.TIME_STOP.get()) || 
                entity instanceof PlayerEntity && canPlayerMoveInStoppedTime((PlayerEntity) entity, false)
                || entity instanceof EndermanEntity); // for the lulz
    }

    
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        sendWorldTimeStopData(player, player.level);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        sendWorldTimeStopData(player, player.server.getLevel(event.getTo()));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        sendWorldTimeStopData(player, player.level);
    }
    
    private static void sendWorldTimeStopData(ServerPlayerEntity player, World world) {
        if (isTimeStopped(world)) {
            boolean canMove = canPlayerMoveInStoppedTime(player, true);
            boolean canSee = canPlayerSeeInStoppedTime(canMove, hasTimeStopAbility(player));
            PacketManager.sendToClient(new SyncWorldTimeStopPacket(canSee, canMove), player);
        }
        else {
            PacketManager.sendToClient(SyncWorldTimeStopPacket.timeResumed(), player);
        }
    }
    
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        event.world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isTimeStopped()) {
                cap.decTimeStopTicks();
                if (!cap.isTimeStopped()) {
                    resumeTime(event.world, false);
                }
            }
        });
    }
    
    
    
    @SubscribeEvent
    public static void onTSEffectAdded(PotionAddedEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.getOldPotionEffect() == null && event.getPotionEffect().getEffect() == ModEffects.TIME_STOP.get() && isTimeStopped(entity.level)) {
            updateEntityTimeStop(entity, true, false);
            if (!entity.level.isClientSide()) {
                ((ServerWorld) entity.level).getChunkSource().broadcast(entity, (new SPlayEntityEffectPacket(entity.getId(), event.getPotionEffect())));
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), true), entity);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTSEffectExpired(PotionExpiryEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.getPotionEffect().getEffect() == ModEffects.TIME_STOP.get() && isTimeStopped(entity.level)) {
            updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), false), entity);
                if (entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().get().getTimeStopTicks() >= 40 && 
                        IStandPower.getStandPowerOptional(entity).map(stand -> stand.getType() == ModStandTypes.THE_WORLD.get()).orElse(false)) {
                    JojoModUtil.sayVoiceLine(entity, ModSounds.DIO_CANT_MOVE.get());
                };
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTSEffectRemoved(PotionRemoveEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.getPotion() == ModEffects.TIME_STOP.get() && isTimeStopped(entity.level)) {
            updateEntityTimeStop(entity, false, false);
            if (!entity.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new RefreshMovementInTimeStopPacket(entity.getId(), false), entity);
            }
        }
    }
    
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelBlockNeighborUpdate(NeighborNotifyEvent event) {
        if (isTimeStopped((World) event.getWorld())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelFluidSpread(FluidPlaceBlockEvent event) {
        if (isTimeStopped((World) event.getWorld())) {
            event.setNewState(event.getOriginalState());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelFluidSourceCreation(CreateFluidSourceEvent event) {
        if (isTimeStopped((World) event.getWorld())) {
            event.setResult(Result.DENY);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelCropGrowth(CropGrowEvent.Pre event) {
        if (isTimeStopped((World) event.getWorld())) {
            event.setResult(Result.DENY);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelPistonMovement(PistonEvent.Pre event) {
        if (isTimeStopped((World) event.getWorld())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelNoteBlock(NoteBlockEvent.Play event) {
        if (isTimeStopped((World) event.getWorld())) {
            event.setCanceled(true);
        }
    }
    
    
    
    public static boolean isTimeStopped(World world) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).map(cap -> cap.isTimeStopped()).orElse(false);
    }
    
    public static int getTimeStopTicks(int minStandExp, IStandPower standPower, LivingEntity user, LazyOptional<INonStandPower> otherPower) {
        float ticks = (float) (standPower.getExp() - minStandExp) / (float) (IStandPower.MAX_EXP - minStandExp) * 95F + 5;
        ticks *= otherPower.map(power -> {
           if (power.getType() == ModNonStandPowers.VAMPIRISM.get()) {
               return (float) MathHelper.clamp(VampirismPowerType.bloodLevel(power, user.level.getDifficulty()), 3, 6) / 3F;
           }
           return 1F;
        }).orElse(1F);
        return MathHelper.floor(ticks);
    }
    
    
    
    public static void addAllowMovingInStoppedTime(Action action) {
        if (ALLOW_MOVING_IN_STOPPED_TIME == null) {
            TIME_STOP_ACTIONS_BUILDER.add(action);
        }
    }
    
    public static void initTimeManipulatingActions() {
        ALLOW_MOVING_IN_STOPPED_TIME = TIME_STOP_ACTIONS_BUILDER.build();
    }
}
