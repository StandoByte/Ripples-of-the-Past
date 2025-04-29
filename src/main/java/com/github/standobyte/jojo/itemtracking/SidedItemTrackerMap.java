package com.github.standobyte.jojo.itemtracking;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class SidedItemTrackerMap {
    final Map<UUID, TrackerItemStack> trackingMap = new HashMap<>();
    final Set<UUID> serverTrackedIds = new HashSet<>();
    
    public static SidedItemTrackerMap getSidedTrackers(World world) {
        if (!world.isClientSide()) {
            return SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker();
        }
        else {
            return ClientUtil.clientTrackedItems;
        }
    }
    
    public static void tick(MinecraftServer server) {
        Iterator<TrackerItemStack> iter = SaveFileUtilCapProvider.getSaveFileCap(server).getItemsTracker().trackingMap.values().iterator();
        while (iter.hasNext()) {
            TrackerItemStack tracker = iter.next();
            if (!tracker.isTracked()) {
                iter.remove();
            }
            else {
                tracker.tick(server);
            }
        }
    }
    
    public void addServerTrackedId(UUID id) {
        serverTrackedIds.add(id);
    }
    
    public void updateTracker(UUID id, TrackerItemStack tracker, World world) {
        if (world.isClientSide() || serverTrackedIds.contains(id)) {
            TrackerItemStack prev = trackingMap.put(id, tracker);
            if (prev != null && prev != tracker) {
                prev.clear();
            }
        }
        else {
            tracker.clear();
            trackingMap.remove(id);
        }
    }
    
    public void removeTracker(UUID id) {
        serverTrackedIds.remove(id);
        TrackerItemStack tracker = trackingMap.get(id);
        if (tracker != null) {
            tracker.clear();
            trackingMap.remove(id);
        }
    }
    
    public TrackerItemStack getTracker(UUID id) {
        return trackingMap.get(id);
    }
    
    public Collection<TrackerItemStack> values() {
        return trackingMap.values();
    }
    
}
