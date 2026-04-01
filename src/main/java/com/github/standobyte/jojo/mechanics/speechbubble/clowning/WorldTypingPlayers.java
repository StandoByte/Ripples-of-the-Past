package com.github.standobyte.jojo.mechanics.speechbubble.clowning;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.network.PacketManager;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WorldTypingPlayers {
    public static final Random random = new Random();
    public final World world;
    public Set<UUID> players = new LinkedHashSet<>();
    public Object2IntMap<UUID> notificationIndex = new Object2IntArrayMap<>();
    
    public WorldTypingPlayers(World world) {
        this.world = world;
    }
    
    public void setFor(UUID playerId, boolean state) {
        if (state) {
            players.add(playerId);
            notificationIndex.put(playerId, randomIndex());
        }
        else {
            players.remove(playerId);
            notificationIndex.removeInt(playerId);
        }
    }
    
    public static int randomIndex() {
        return random.nextInt(6) + 1;
    }
    
    public void filterRemovedPlayers() {
        Iterator<UUID> iter = players.iterator();
        while (iter.hasNext()) {
            UUID uuid = iter.next();
            Entity entity = world.getPlayerByUUID(uuid);
            if (entity == null || !entity.isAlive()) {
                iter.remove();
                notificationIndex.removeInt(uuid);
            }
        }
    }
    
    
    public static boolean hasInRange(World world, Vector3d pos) {
        WorldTypingPlayers tracker = WorldTypingPlayers.get(world);
        if (tracker != null) {
            return tracker.players.stream().map(uuid -> world.getPlayerByUUID(uuid))
                    .anyMatch(player -> isInRange(player, pos));
        }
        return false;
    }
    
    public static boolean hasAny(World world) {
        WorldTypingPlayers tracker = WorldTypingPlayers.get(world);
        return tracker != null && !tracker.players.isEmpty();
    }
    
    public static boolean isInRange(Entity typingEntity, Vector3d pos) {
        if (typingEntity == null) return false;
        double distanceSqr = 32;
        distanceSqr *= distanceSqr;
        return typingEntity.position().distanceToSqr(pos) <= distanceSqr;
    }
    
    public static boolean isTyping(Entity entity) {
        WorldTypingPlayers tracker = WorldTypingPlayers.get(entity.level);
        if (tracker != null) {
            return tracker.players.contains(entity.getUUID());
        }
        return false;
    }
    
    
    public static WorldTypingPlayers get(World world) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY).map(cap -> cap.peepoClown).orElse(null);
    }
    
    
    public static void clientSideChangedState(PlayerEntity clientPlayer, boolean state) {
        if (clientPlayer != null && clientPlayer.level != null) {
            World level = clientPlayer.level;
            WorldTypingPlayers clientSideTracker = WorldTypingPlayers.get(level);
            if (clientSideTracker != null) {
                clientSideTracker.setFor(clientPlayer.getUUID(), state);
                PacketManager.sendToServer(ClSpeechChatStatePacket.of(state));
            }
        }
    }
    
    @ApiStatus.Internal
    public static void serverSideStatePacket(PlayerEntity senderPlayer, boolean state) {
        if (senderPlayer != null && senderPlayer.level != null) {
            World level = senderPlayer.level;
            WorldTypingPlayers serverSideTracker = WorldTypingPlayers.get(level);
            if (serverSideTracker != null) {
                serverSideTracker.setFor(senderPlayer.getUUID(), state);
                RegistryKey<World> dimension = level.dimension();
                PacketManager.sendGlobally(new SpeechChatTypingPlayersPacket(dimension, serverSideTracker.players), dimension);
            }
        }
    }

    @ApiStatus.Internal
    public static void clientSideReceivedState(World world, Collection<UUID> typingPlayers) {
        if (world != null) {
            WorldTypingPlayers clientSideTracker = WorldTypingPlayers.get(world);
            if (clientSideTracker != null) {
                ObjectIterator<Object2IntMap.Entry<UUID>> iter = clientSideTracker.notificationIndex.object2IntEntrySet().iterator();
                while (iter.hasNext()) {
                    Object2IntMap.Entry<UUID> entry = iter.next();
                    UUID id = entry.getKey();
                    if (!typingPlayers.contains(id)) {
                        iter.remove();
                    }
                }
                
                clientSideTracker.players.clear();
                clientSideTracker.players.addAll(typingPlayers);
                
                for (UUID newId : typingPlayers) {
                    if (!clientSideTracker.notificationIndex.containsKey(newId)) {
                        clientSideTracker.notificationIndex.put(newId, randomIndex());
                    }
                }
            }
        }
    }
}
