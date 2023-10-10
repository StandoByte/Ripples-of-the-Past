package com.github.standobyte.jojo.capability.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.JojoModConfig.Common;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.NotificationSyncPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDoubleShiftPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonLiquidWalkingPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerContinuousActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrWalkmanEarbudsPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mod.JojoModVersion;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class PlayerUtilCap {
    private final PlayerEntity player;
    
    public int knivesThrewTicks = 0;
    
    public PlayerUtilCap(PlayerEntity player) {
        this.player = player;
    }
    
    
    
    public void tick() {
        if (!player.level.isClientSide()) {
            tickKnivesRemoval();
            tickVoiceLines();
            tickClientInputTimer();
            tickNoSleepTimer();
            
            if (knivesThrewTicks > 0) knivesThrewTicks--;
            if (chatSpamTickCount > 0) chatSpamTickCount--;
        }
        
        tickContinuousAction();
        tickDoubleShift();
    }
    
    public void saveOnDeath(PlayerUtilCap cap) {
        this.notificationsSent = cap.notificationsSent;
    }
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("NotificationsSent", notificationsToNBT());
        nbt.put("RotpVersion", JojoModVersion.getCurrentVersion().toNBT());
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("NotificationsSent", 10)) {
            CompoundNBT notificationsMap = nbt.getCompound("NotificationsSent");
            notificationsFromNBT(notificationsMap);
        }
    }
    
    public void onTracking(ServerPlayerEntity tracking) {
        PacketManager.sendToClient(new TrKnivesCountPacket(player.getId(), knives), tracking);
        PacketManager.sendToClient(new TrWalkmanEarbudsPacket(player.getId(), walkmanEarbuds), tracking);
    }
    
    public void syncWithClient() {
        PacketManager.sendToClient(new NotificationSyncPacket(notificationsSent), (ServerPlayerEntity) player);
    }
    
    
    
    private Optional<ContinuousActionInstance<?, ?>> continuousAction = Optional.empty();
    
    public void setContinuousAction(@Nullable ContinuousActionInstance<?, ?> action) {
        continuousAction = Optional.ofNullable(action);
        if (!player.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrPlayerContinuousActionPacket(
                    player.getId(), continuousAction.map(ContinuousActionInstance::getActionSync)), player);
        }
    }
    
    private void tickContinuousAction() {
        continuousAction.ifPresent(cap -> {
            if (!cap.isStopped()) {
                cap.tick();
            }
            if (!player.level.isClientSide() && cap.isStopped()) {
                stopContinuousAction();
            }
        });
    }
    
    public void stopContinuousAction() {
        continuousAction.ifPresent(action -> action.stopAction());
        setContinuousAction(null);
    }
    
    public Optional<ContinuousActionInstance<?, ?>> getContinuousAction() {
        return continuousAction;
    }
    
    public <T extends ContinuousActionInstance<T, P>, P extends IPower<P, ?>> Optional<T> getContinuousActionIfItIs(IPlayerAction<T, P> action) {
        if (GeneralUtil.orElseFalse(continuousAction.map(ContinuousActionInstance::getAction), currentAction -> currentAction == action)) {
            return (Optional<T>) continuousAction;
        }
        return Optional.empty();
    }
    
    
    
    private boolean doubleShiftPress = false;
    private boolean shiftSynced = false;
    private boolean isWalkingOnLiquid = false;
    private boolean clTickSpark;
    
    public void setDoubleShiftPress() {
        doubleShiftPress = true;
        shiftSynced = player.isShiftKeyDown();
        if (!player.level.isClientSide()) {
            PacketManager.sendToClientsTracking(new TrDoubleShiftPacket(player.getId()), player);
        }
    }
    
    private void tickDoubleShift() {
        if (doubleShiftPress) {
            if (!shiftSynced) {
                if (player.isShiftKeyDown()) {
                    shiftSynced = true;
                }
            }
            else if (!player.isShiftKeyDown()) {
                doubleShiftPress = false;
            }
        }
    }
    
    public boolean getDoubleShiftPress() {
        return doubleShiftPress;
    }
    
    public void setWaterWalking(boolean waterWalking) {
        if (this.isWalkingOnLiquid ^ waterWalking) {
            if (!player.level.isClientSide()) {
                PacketManager.sendToClientsTracking(new TrHamonLiquidWalkingPacket(player.getId(), waterWalking), player);
            }
            else if (waterWalking) {
                HamonUtil.emitHamonSparkParticles(player.level, ClientUtil.getClientPlayer(), player.position(), 0.05F);
                CustomParticlesHelper.createHamonSparkParticles(null, player.position(), 10);
                this.clTickSpark = false;
            }
            this.isWalkingOnLiquid = waterWalking;
        }
    }
    
    public boolean isWaterWalking() {
        return isWalkingOnLiquid;
    }
    
    public void tickWaterWalking() {
        if (player.level.isClientSide() && isWalkingOnLiquid && clTickSpark) {
            HamonSparksLoopSound.playSparkSound(player, player.position(), 1.0F);
            CustomParticlesHelper.createHamonSparkParticles(player, 
                    player.getRandomX(0.5), player.getY(Math.random() * 0.1), player.getRandomZ(0.5), 
                    1);
        }
        clTickSpark = true;
    }
    
    
    
    private Set<OneTimeNotification> notificationsSent = EnumSet.noneOf(OneTimeNotification.class);
    
    public void sendNotification(OneTimeNotification notification, ITextComponent message) {
        if (!sentNotification(notification)) {
            player.sendMessage(message, Util.NIL_UUID);
            setSentNotification(notification, true);
        }
    }
    
    public boolean sentNotification(OneTimeNotification notification) {
        return notificationsSent.contains(notification);
    }
    
    public void setSentNotification(OneTimeNotification notification, boolean sent) {
        if (sent) {
            notificationsSent.add(notification);
        }
        else {
            notificationsSent.remove(notification);
        }
        if (!player.level.isClientSide()) {
            PacketManager.sendToClient(new NotificationSyncPacket(notificationsSent), (ServerPlayerEntity) player);
        }
    }
    
    public static enum OneTimeNotification {
        POWER_CONTROLS,
        HAMON_WINDOW,
        HAMON_BREATH_GUIDE,
        HIGH_STAND_RANGE;
    }
    
    public void notificationsFromNBT(CompoundNBT nbt) {
        notificationsSent.clear();
        for (OneTimeNotification flag : OneTimeNotification.values()) {
            if (nbt.getBoolean(flag.name())) {
                notificationsSent.add(flag);
            }
        }
    }
    
    public CompoundNBT notificationsToNBT() {
        CompoundNBT notificationsMap = new CompoundNBT();
        for (OneTimeNotification flag : OneTimeNotification.values()) {
            notificationsMap.putBoolean(flag.name(), sentNotification(flag));
        }
        return notificationsMap;
    }
    
    
    
    private int knives;
    private int removeKnifeTime;
    
    public void setKnives(int knives) {
        knives = Math.max(knives, 0);
        if (this.knives != knives) {
            this.knives = knives;
            if (!player.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrKnivesCountPacket(player.getId(), knives), player);
            }
        }
    }
    
    public void addKnife() {
        setKnives(knives + 1);
    }
    
    public int getKnivesCount() {
        return knives;
    }
    
    private void tickKnivesRemoval() {
        if (knives > 0) {
            if (removeKnifeTime <= 0) {
                removeKnifeTime = 20 * (30 - knives);
            }
            removeKnifeTime--;
            if (removeKnifeTime <= 0) {
                setKnives(knives - 1);
            }
        }
    }
    
    
    
    private boolean hasClientInput;
    private int noClientInputTimer;
    
    public void setHasClientInput(boolean hasInput) {
        this.hasClientInput = hasInput;
        if (hasClientInput) {
            noClientInputTimer = 0;
        }
    }
    
    private void tickClientInputTimer() {
        if (!hasClientInput) {
            noClientInputTimer++;
        }
    }
    
    public boolean hasClientInput() {
        return hasClientInput;
    }
    
    public int getNoClientInputTimer() {
        return noClientInputTimer;
    }
    
    
    
    public int getStandXpLevelsRequirement(boolean clientSide) {
        Common config = JojoModConfig.getCommonConfigInstance(clientSide);
        return config.standXpCostInitial.get() + config.standXpCostIncrease.get() * 5;
    }
    
    
    
    private BedType lastBedType;
    private int ticksNoSleep;
    private long nextSleepTime;
    
    private void tickNoSleepTimer() {
        if (ticksNoSleep > 0) ticksNoSleep--;
    }
    
    public void onSleep(boolean isCoffin, int ticksSkipped) {
        this.lastBedType = isCoffin ? BedType.COFFIN : BedType.BED;
        this.ticksNoSleep = ticksSkipped * 2;
        this.nextSleepTime = player.level.dayTime() + ticksNoSleep;
    }
    
    public boolean canGoToSleep(boolean isCoffin) {
        return 
                this.lastBedType == null || 
                !this.lastBedType.isCoffin && !isCoffin || 
                ticksNoSleep <= 0 || 
                nextSleepTime < player.level.dayTime();
    }
    
    private static enum BedType {
        BED(false),
        COFFIN(true);
        
        private final boolean isCoffin;
        
        private BedType(boolean isCoffin) {
            this.isCoffin = isCoffin;
        }
    }
    
    
    
    private Optional<RockPaperScissorsGame> currentGame = Optional.empty();
    
    public Optional<RockPaperScissorsGame> getCurrentRockPaperScissorsGame() {
        return currentGame;
    }
    
    public void setCurrentRockPaperScissorsGame(RockPaperScissorsGame game) {
        this.currentGame = game != null ? Optional.of(game) : Optional.empty();
    }
    
    
    
    private boolean walkmanEarbuds = false;
    
    public void setEarbuds(boolean earbuds) {
        if (this.walkmanEarbuds != earbuds) {
            this.walkmanEarbuds = earbuds;
            if (!player.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrWalkmanEarbudsPacket(player.getId(), walkmanEarbuds), player);
            }
        }
    }
    
    public boolean hasEarbuds() {
        return walkmanEarbuds;
    }
    
    
    
    private int chatSpamTickCount = 0;
    
    public void onChatMsgBypassingSpamCheck(MinecraftServer server, ServerPlayerEntity serverPlayer) {
        chatSpamTickCount += 20;
        if (chatSpamTickCount > 200 && !server.getPlayerList().isOp(player.getGameProfile())) {
            serverPlayer.connection.disconnect(new TranslationTextComponent("disconnect.spam"));
        }
    }
    
    
    
    private Map<SoundEvent, Integer> recentlyPlayedVoiceLines = new HashMap<>();
    
    @Nullable
    public boolean checkNotRepeatingVoiceLine(SoundEvent voiceLine, int voiceLineDelay) {
        if (recentlyPlayedVoiceLines.containsKey(voiceLine) && recentlyPlayedVoiceLines.get(voiceLine) > 0) {
            return false;
        }
        recentlyPlayedVoiceLines.put(voiceLine, voiceLineDelay);
        return true;
    }
    
    private void tickVoiceLines() {
        for (Map.Entry<SoundEvent, Integer> voiceLine : recentlyPlayedVoiceLines.entrySet()) {
            int ticks = voiceLine.getValue();
            if (ticks > 0) {
                voiceLine.setValue(--ticks);
            }
        }
    }

}
