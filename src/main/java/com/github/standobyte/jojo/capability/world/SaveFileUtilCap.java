package com.github.standobyte.jojo.capability.world;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.mob.rps.RPSPvpGamesMap;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.item.polaroid.PhotosHandler;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ServerIdPacket;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

public class SaveFileUtilCap {
    private final ServerWorld overworld;

    private UUID serverId;
    
    private Map<StandType<?>, Integer> timesStandsTaken = new HashMap<>();
    
    private final RPSPvpGamesMap rpsPvpGames = new RPSPvpGamesMap();
    
    private boolean gameruleDayLightCycle;
    private boolean gameruleWeatherCycle;
    private boolean usedTimeStop = false;
    private boolean refreshNextTick = false;
    
    private int walkmanId;
    private int cassetteId;
    
    private final PhotosHandler polaroidPhotos;
    
    public SaveFileUtilCap(ServerWorld overworld) {
        this.overworld = overworld;
        if (!overworld.isClientSide()) {
            this.serverId = UUID.randomUUID();
        }
        this.polaroidPhotos = new PhotosHandler(overworld.getServer());
    }
    
    public UUID getServerUUID() {
        return serverId;
    }
    
    public void tick() {
        if (refreshNextTick) {
            if (usedTimeStop) {
                GameRules gameRules = overworld.getGameRules();
                MinecraftServer server = overworld.getServer();
                gameRules.getRule(GameRules.RULE_DAYLIGHT).set(gameruleDayLightCycle, server);
                gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(gameruleWeatherCycle, server);
                usedTimeStop = false;
            }
            refreshNextTick = false;
        }
    }
    
    public void onPlayerLogIn(ServerPlayerEntity player) {
        PacketManager.sendToClient(new ServerIdPacket(serverId), player);
    }
    
    public void addPlayerStand(StandType<?> type) {
        int prevValue = timesStandsTaken.containsKey(type) ? timesStandsTaken.get(type) : 0;
        timesStandsTaken.put(type, ++prevValue);
    }
    
    public void removePlayerStand(StandType<?> type) {
        if (timesStandsTaken.containsKey(type)) {
            int prevValue = timesStandsTaken.get(type);
            if (prevValue <= 1) {
                timesStandsTaken.remove(type);
            }
            else {
                timesStandsTaken.put(type, --prevValue);
            }
        }
    }
    
    public List<StandType<?>> getNotTakenStands(List<StandType<?>> fromStands) {
        if (fromStands.isEmpty()) {
            return fromStands;
        }
        return fromStands
                .stream()
                .filter(stand -> !timesStandsTaken.containsKey(stand) || timesStandsTaken.get(stand) <= 0)
                .collect(Collectors.toList());
    }
    
    public List<StandType<?>> getLeastTakenStands(List<StandType<?>> fromStands) {
        if (fromStands.isEmpty()) {
            return fromStands;
        }
        Set<Map.Entry<StandType<?>, Integer>> entriesRequested = timesStandsTaken
                .entrySet()
                .stream()
                .filter(entry -> fromStands.contains(entry.getKey()))
                .collect(Collectors.toSet());
        if (entriesRequested.size() == fromStands.size()) {
            int min = Collections.min(entriesRequested, Comparator.comparingInt(entry -> entry.getValue())).getValue();
            return entriesRequested
                    .stream()
                    .filter(entry -> entry.getValue() == min)
                    .map(entry -> entry.getKey())
                    .collect(Collectors.toList());
        }
        else { // means there are stands with no record of being given to someone
            List<StandType<?>> st = entriesRequested.stream().map(Map.Entry::getKey).collect(Collectors.toList());
            return fromStands
                    .stream()
                    .filter(stand -> !st.contains(stand))
                    .collect(Collectors.toList());
        }
    }
    
    
    
    public RPSPvpGamesMap getPvpRPSGames() {
        return rpsPvpGames;
    }
    
    
    
    public void setTimeStopGamerules(ServerWorld world) {
        if (noTimeStopInstances(world)) {
            GameRules gameRules = overworld.getGameRules();
            MinecraftServer server = overworld.getServer();
            gameruleDayLightCycle = gameRules.getBoolean(GameRules.RULE_DAYLIGHT);
            gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            gameruleWeatherCycle = gameRules.getBoolean(GameRules.RULE_WEATHER_CYCLE);
            gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
            usedTimeStop = true;
        }
    }
    
    public void restoreTimeStopGamerules(ServerWorld world) {
        if (usedTimeStop && noTimeStopInstances(world)) {
            GameRules gameRules = overworld.getGameRules();
            MinecraftServer server = overworld.getServer();
            gameRules.getRule(GameRules.RULE_DAYLIGHT).set(gameruleDayLightCycle, server);
            gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(gameruleWeatherCycle, server);
            usedTimeStop = false;
        }
    }
    
    private boolean noTimeStopInstances(@Nullable ServerWorld except) {
        MinecraftServer server = overworld.getServer();
        for (ServerWorld world : server.getAllLevels()) {
            if (world != except && world.getCapability(WorldUtilCapProvider.CAPABILITY)
                    .map(cap -> cap.getTimeStopHandler().hasTimeStopInstances()).orElse(false)) {
                return false;
            }
        }
        
        return true;
    }
    
    
    
    public int incWalkmanId() {
        return ++walkmanId;
    }
    
    public int incCassetteId() {
        return ++cassetteId;
    }
    
    public PhotosHandler getPolaroidPhotos() {
        return polaroidPhotos;
    }
    
    
    
    CompoundNBT save() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("ServerId", serverId);
        
        CompoundNBT timesStandsTakenMap = new CompoundNBT();
        for (Map.Entry<StandType<?>, Integer> entry : timesStandsTaken.entrySet()) {
            timesStandsTakenMap.putInt(JojoCustomRegistries.STANDS.getKeyAsString(entry.getKey()), entry.getValue());
        }
        nbt.put("StandsTaken", timesStandsTakenMap);
        
        nbt.putBoolean("GameruleDayLightCycle", gameruleDayLightCycle);
        nbt.putBoolean("GameruleWeatherCycle", gameruleWeatherCycle);
        nbt.putBoolean("UsedTimeStop", usedTimeStop);
        nbt.putInt("WalkmanId", walkmanId);
        nbt.putInt("CassetteId", cassetteId);
        nbt.put("PolaroidPhotos", polaroidPhotos.toNBT());
        nbt.putInt("StandEffId", StandEffectsTracker.EFFECTS_COUNTER.get());
        return nbt;
    }
    
    void load(CompoundNBT nbt) {
        if (nbt.hasUUID("ServerId")) {
            serverId = nbt.getUUID("ServerId");
        }
        
        if (nbt.contains("StandsTaken", 10)) {
            Map<StandType<?>, Integer> stands = new HashMap<>();
            CompoundNBT timesStandsTakenNBT = nbt.getCompound("StandsTaken");
            JojoCustomRegistries.STANDS.getRegistry().forEach(stand -> {
                int timesTaken = timesStandsTakenNBT.getInt(JojoCustomRegistries.STANDS.getKeyAsString(stand));
                if (timesTaken > 0) {
                    stands.put(stand, timesTaken);
                }
            });
            timesStandsTaken = stands;
        }
        
        usedTimeStop = nbt.getBoolean("UsedTimeStop");
        refreshNextTick = usedTimeStop;
        gameruleDayLightCycle = nbt.getBoolean("GameruleDayLightCycle");
        gameruleWeatherCycle = nbt.getBoolean("GameruleWeatherCycle");
        walkmanId = nbt.getInt("WalkmanId");
        cassetteId = nbt.getInt("CassetteId");
        if (nbt.contains("PolaroidPhotos", Constants.NBT.TAG_COMPOUND)) polaroidPhotos.fromNBT(nbt.getCompound("PolaroidPhotos"));
        int latestId = nbt.getInt("StandEffId");
        if (latestId < (1 << 30)) {
            StandEffectsTracker.EFFECTS_COUNTER.set(latestId);
        }
    }
}
