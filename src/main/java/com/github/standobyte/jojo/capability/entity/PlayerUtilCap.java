package com.github.standobyte.jojo.capability.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.block.WoodenCoffinBlock;
import com.github.standobyte.jojo.capability.entity.player.PlayerClientBroadcastedSettings;
import com.github.standobyte.jojo.capability.entity.player.PlayerMixinExtension;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.NotificationSyncPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDoubleShiftPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerContinuousActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerVisualDetailPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrWalkmanEarbudsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.VampireSleepInCoffinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.MetEntityTypesPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.PlayerStatListener;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class PlayerUtilCap {
    private final PlayerEntity player;
    private final PlayerMixinExtension playerMixin;
    
    private PlayerClientBroadcastedSettings broadcastedSettings = new PlayerClientBroadcastedSettings();
    
    public int knivesThrewTicks = 0;
    
    private final Map<Entity, Map<DataParameter<?>, EntityDataManager.DataEntry<?>>> tsDelayedData = new HashMap<>();
    
    private Optional<ContinuousActionInstance<?, ?>> continuousAction = Optional.empty();
    
    private boolean doubleShiftPress = false;
    private boolean shiftSynced = false;
    
    private Set<OneTimeNotification> notificationsSent = new HashSet<>();
    
    private int knives;
    private int removeKnifeTime;
    
    private int ateInkPastaTicks = 0;
    
    private boolean hasClientInput;
    private int noClientInputTimer;
    
    public boolean coffinPreventDayTimeSkip = false;
    
    private Set<ResourceLocation> metEntityTypesId = new HashSet<>();
    
    private Optional<RockPaperScissorsGame> currentGame = Optional.empty();
    
    private boolean walkmanEarbuds = false;
    
    private int chatSpamTickCount = 0;
    
    private Map<SoundEvent, Integer> recentlyPlayedVoiceLines = new HashMap<>();
    
    private final List<PlayerStatListener<?>> statChangeListeners = new ArrayList<>();
    private final List<TimedAction> sendWhenScreenClosed = new ArrayList<>();
    
    
    
    public PlayerUtilCap(PlayerEntity player) {
        this.player = player;
        this.playerMixin = player instanceof PlayerMixinExtension ? (PlayerMixinExtension) player : null;
    }
    
    
    
    public void tick() {
        if (!player.level.isClientSide()) {
            tickKnivesRemoval();
            tickVoiceLines();
            tickClientInputTimer();
            tickStatUpdates();
            tickQueuedOnScreenClose();
            
            if (knivesThrewTicks > 0) knivesThrewTicks--;
            if (chatSpamTickCount > 0) chatSpamTickCount--;
        }

        if (ateInkPastaTicks > 0) --ateInkPastaTicks;
        tickCoffinSleepTimer();
        tickContinuousAction();
        tickDoubleShift();
    }
    
    public void onClone(PlayerUtilCap old, boolean wasDeath) {
        this.notificationsSent = old.notificationsSent;
        this.broadcastedSettings = old.broadcastedSettings;
    }
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("NotificationsSent", notificationsToNBT());
        
        if (!metEntityTypesId.isEmpty()) {
            ListNBT metEntities = new ListNBT();
            metEntityTypesId.forEach(entityTypeId -> metEntities.add(StringNBT.valueOf(entityTypeId.toString())));
            nbt.put("MetEntityTypes", metEntities);
        }
        
        nbt.putBoolean("CoffinRespawn", coffinPreventDayTimeSkip);
        
        playerMixin.toNBT(nbt);
        return nbt;
    }



    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("NotificationsSent", 10)) {
            CompoundNBT notificationsMap = nbt.getCompound("NotificationsSent");
            notificationsFromNBT(notificationsMap);
        }
        
        if (nbt.contains("MetEntityTypes", MCUtil.getNbtId(ListNBT.class))) {
            ListNBT metEntitiesId = nbt.getList("MetEntityTypes", MCUtil.getNbtId(StringNBT.class));
            metEntitiesId.forEach(idNBT -> {
                String idString = ((StringNBT) idNBT).getAsString(); 
                if (!idString.isEmpty()) {
                    ResourceLocation registryName = new ResourceLocation(idString);
                    metEntityTypesId.add(registryName);
                }
            });
        }
        
        coffinPreventDayTimeSkip = nbt.getBoolean("CoffinRespawn");
        
        playerMixin.fromNBT(nbt);
    }
    
    public void onTracking(ServerPlayerEntity tracking) {
        broadcastedSettings.syncToTracking(player, tracking);
        PacketManager.sendToClient(new TrKnivesCountPacket(player.getId(), knives), tracking);
        PacketManager.sendToClient(new TrWalkmanEarbudsPacket(player.getId(), walkmanEarbuds), tracking);
        PacketManager.sendToClient(new TrPlayerVisualDetailPacket(player.getId(), ateInkPastaTicks), tracking);
        playerMixin.syncToTracking(tracking);
    }
    
    public void syncWithClient() {
        ServerPlayerEntity player = (ServerPlayerEntity) this.player;
        PacketManager.sendToClient(new NotificationSyncPacket(notificationsSent), player);
        PacketManager.sendToClient(new TrKnivesCountPacket(player.getId(), knives), player);
        PacketManager.sendToClient(new TrWalkmanEarbudsPacket(player.getId(), walkmanEarbuds), player);
        PacketManager.sendToClient(new TrPlayerVisualDetailPacket(player.getId(), ateInkPastaTicks), player);
        PacketManager.sendToClient(new VampireSleepInCoffinPacket(coffinPreventDayTimeSkip), player);
        if (!metEntityTypesId.isEmpty()) {
            PacketManager.sendToClient(new MetEntityTypesPacket(metEntityTypesId), player);
        }
        playerMixin.syncToClient(player);
    }
    
    
    public static class OneTimeNotification {
        private static final List<OneTimeNotification> VALUES = new ArrayList<>();
        
        public static final OneTimeNotification POWER_CONTROLS = new OneTimeNotification("POWER_CONTROLS");
        public static final OneTimeNotification HAMON_WINDOW = new OneTimeNotification("HAMON_WINDOW");
        public static final OneTimeNotification HAMON_BREATH_GUIDE = new OneTimeNotification("HAMON_BREATH_GUIDE");
        public static final OneTimeNotification HIGH_STAND_RANGE = new OneTimeNotification("HIGH_STAND_RANGE");
        public static final OneTimeNotification BOUGHT_METEORITE_MAP = new OneTimeNotification("BOUGHT_METEORITE_MAP");
        public static final OneTimeNotification BOUGHT_HAMON_TEMPLE_MAP = new OneTimeNotification("BOUGHT_HAMON_TEMPLE_MAP");
        public static final OneTimeNotification BOUGHT_PILLAR_MAN_TEMPLE_MAP = new OneTimeNotification("BOUGHT_PILLAR_MAN_TEMPLE_MAP");
        
        private final String name;
        
        private OneTimeNotification(String name) {
            this.name = name;
            VALUES.add(this);
        }
        
        public String name() {
            return name;
        }
        
        public static List<OneTimeNotification> values() {
            return VALUES;
        }
        
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OneTimeNotification) {
                return this.name.equals(((OneTimeNotification) obj).name);
            }
            return false;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    
    public PlayerClientBroadcastedSettings getBroadcastedSettings() {
        return broadcastedSettings;
    }
    
    
    public void addDataForTSUnfreeze(Entity entity, Iterable<EntityDataManager.DataEntry<?>> newData) {
        Map<DataParameter<?>, EntityDataManager.DataEntry<?>> data = tsDelayedData.computeIfAbsent(entity, e -> new HashMap<>());
        for (EntityDataManager.DataEntry<?> dataEntry : newData) {
            data.put(dataEntry.getAccessor(), dataEntry);
        }
    }
    
    public void sendDataOnTSUnfreeze() {
        if (player.level.isClientSide()) {
            return;
        }
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        if (!tsDelayedData.isEmpty()) {
            tsDelayedData.forEach((entity, data) -> {
                if (!data.isEmpty()) {
                    PacketManager.sendToClient(new TrDirectEntityDataPacket(entity.getId(), new ArrayList<>(data.values())), serverPlayer);
                }
            });
        }
        
        tsDelayedData.clear();
    }
    
    
    
    public void setContinuousAction(@Nullable ContinuousActionInstance<?, ?> action) {
        continuousAction.ifPresent(ContinuousActionInstance::stopAction);
        if (action != null) {
            action.onStart();
        }
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
    
    public <T extends ContinuousActionInstance<?, P>, P extends IPower<P, ?>> Optional<T> getContinuousActionIfItIs(IPlayerAction<T, P> action) {
        if (GeneralUtil.orElseFalse(continuousAction.map(ContinuousActionInstance::getAction), currentAction -> currentAction == action)) {
            return (Optional<T>) continuousAction;
        }
        return Optional.empty();
    }
    
    
    
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
    

    
    public int getInkPastaVisuals() {
        return ateInkPastaTicks;
    }
    
    public void setInkPastaVisuals() {
        setInkPastaVisuals(600);
    }
    
    public void setInkPastaVisuals(int ticks) {
        this.ateInkPastaTicks = ticks;
        if (!player.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrPlayerVisualDetailPacket(player.getId(), ateInkPastaTicks), player);
        }
    }
    
    
    
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
    
    
    
    public void onSleepingInCoffin(boolean isVampireRespawning) {
        this.coffinPreventDayTimeSkip = isVampireRespawning;
        if (!player.level.isClientSide()) {
            PacketManager.sendToClient(new VampireSleepInCoffinPacket(isVampireRespawning), (ServerPlayerEntity) player);
        }
    }
    
    public void onWakeUp() {
        this.coffinPreventDayTimeSkip = false;
        if (!player.level.isClientSide()) {
            PacketManager.sendToClient(new VampireSleepInCoffinPacket(false), (ServerPlayerEntity) player);
        }
    }
    
    private void tickCoffinSleepTimer() {
        if (coffinPreventDayTimeSkip && WoodenCoffinBlock.isSleepingInCoffin(player) && !VampirismUtil.isSunny(player.level)) {
            CommonReflection.setSleepCounter(player, 0);
        }
    }
    
    
    
    public Optional<RockPaperScissorsGame> getCurrentRockPaperScissorsGame() {
        return currentGame;
    }
    
    public void setCurrentRockPaperScissorsGame(RockPaperScissorsGame game) {
        this.currentGame = game != null ? Optional.of(game) : Optional.empty();
    }
    
    
    
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
    
    
    
    public void onChatMsgBypassingSpamCheck(MinecraftServer server, ServerPlayerEntity serverPlayer) {
        chatSpamTickCount += 20;
        if (chatSpamTickCount > 200 && !server.getPlayerList().isOp(player.getGameProfile())) {
            serverPlayer.connection.disconnect(new TranslationTextComponent("disconnect.spam"));
        }
    }
    
    
    
    public boolean addMetEntityType(EntityType<?> entityType) {
        return metEntityTypesId.add(entityType.getRegistryName());
    }
    
    public boolean metEntityType(EntityType<?> entityType) {
        return metEntityTypesId.contains(entityType.getRegistryName());
    }
    
    public void addMetEntityTypeId(ResourceLocation id) {
        metEntityTypesId.add(id);
    }
    
    
    
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
    
    
    
    private void tickStatUpdates() {
        statChangeListeners.forEach(PlayerStatListener::tick);
    }
    
    
    
    public void doWhen(Runnable action, BooleanSupplier when) {
        if (when.getAsBoolean()) {
            action.run();
        }
        else {
            sendWhenScreenClosed.add(new TimedAction(action, when));
        }
    }
    
    private void tickQueuedOnScreenClose() {
        if (!sendWhenScreenClosed.isEmpty()) {
            Iterator<TimedAction> it = sendWhenScreenClosed.iterator();
            while (it.hasNext()) {
                TimedAction action = it.next();
                if (action.tryRun()) {
                    it.remove();
                }
            }
        }
    }
    
    private static class TimedAction {
        private final Runnable action;
        private final BooleanSupplier timing;
        
        public TimedAction(Runnable action, BooleanSupplier timing) {
            this.action = action;
            this.timing = timing;
        }
        
        public boolean tryRun() {
            if (timing.getAsBoolean()) {
                action.run();
                return true;
            }
            return false;
        }
    }

}
