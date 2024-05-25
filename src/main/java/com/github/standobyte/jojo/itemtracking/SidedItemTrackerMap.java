package com.github.standobyte.jojo.itemtracking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SidedItemTrackerMap {
    final Map<UUID, TrackerItemStack> trackingMap = new HashMap<>();
    
    public static SidedItemTrackerMap getSidedTrackers(World world) {
        if (!world.isClientSide()) {
            return SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker();
        }
        else {
            return ClientUtil.clientTrackedItems;
        }
    }
    
    public static void tick(ServerWorld world) {
        for (TrackerItemStack tracker : getSidedTrackers(world).trackingMap.values()) {
            tracker.tick(world);
        }
    }
    
    public void updateTracker(UUID id, TrackerItemStack itemCap, World world) {
        TrackerItemStack prev = trackingMap.put(id, itemCap);
        if (prev != null && prev != itemCap) {
            prev.clear();
        }
    }
    
    public void removeTracker(UUID id) {
        trackingMap.remove(id);
    }
    
    public TrackerItemStack getTracker(UUID id) {
        return trackingMap.get(id);
    }
    
    public Collection<TrackerItemStack> values() {
        return trackingMap.values();
    }
    
}
