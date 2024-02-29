package com.github.standobyte.jojo.itemtracking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class SidedItemTrackerMap {
    private final Map<UUID, TrackerItemStack> trackingMap = new HashMap<>();
    
    public void addTracker(UUID id, TrackerItemStack itemCap) {
        trackingMap.put(id, itemCap);
    }
    
    public void removeTracker(UUID id) {
        trackingMap.remove(id);
    }
    
    public Collection<TrackerItemStack> values() {
        return trackingMap.values();
    }
    
    public void test(World world, ServerPlayerEntity sendTo) {
        if (!world.isClientSide()) {
            trackingMap.forEach((id, tracker) -> {
                ITextComponent message;
                Entity entity = tracker.getAtEntity(world);
                if (entity != null) {
                    message = new StringTextComponent(String.format("    %s at %f.2 %f.2 %f.2 (%s)", 
                            id.toString(), entity.getX(), entity.getY(), entity.getZ(), entity.getDisplayName().getString()));
                }
                else {
                    BlockPos pos = tracker.getAtBlockPos(world);
                    if (pos != null) {
                        BlockState blockState = world.isLoaded(pos) ? world.getBlockState(pos) : null;
                        message = new StringTextComponent(String.format("    %s at %d %d %d (%s)", 
                                id.toString(), pos.getX(), pos.getY(), pos.getZ(), 
                                blockState != null ? blockState.getBlock().getName().getString() : "block not loaded"));
                    }
                    else {
                        message = new StringTextComponent(String.format("    %s not found", id.toString()));
                    }
                }
                
                sendTo.sendMessage(message, Util.NIL_UUID);
            });
        }
    }
}
