package com.github.standobyte.jojo.itemtracking;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ItemTrackingEventHandler {
    
    @SubscribeEvent
    public static void trackItemInItemEntity(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level.isClientSide() && entity instanceof ItemEntity) {
            ItemStack item = ((ItemEntity) entity).getItem();
            TrackerItemStack.updateItemAtEntity(entity.level, item, entity.getId());
        }
    }
    
}
