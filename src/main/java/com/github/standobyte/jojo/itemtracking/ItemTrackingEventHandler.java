package com.github.standobyte.jojo.itemtracking;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
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
    
    
    
    
    
    @SubscribeEvent
    public static void pickUpTestItem(LivingUpdateEvent event) {
        if (event.getEntity() instanceof MobEntity && event.getEntity().isAlive()) {
            MobEntity mob = (MobEntity) event.getEntity();
            for (ItemEntity itemEntity : mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(4.0D, 1.0D, 4.0D))) {
                ItemStack itemStack = itemEntity.getItem();
                if ((itemStack.getItem() == ModItems.TMP_ITEM_TRACKING_TEST.get()) &&
                        itemEntity.isAlive() && !itemEntity.getItem().isEmpty() && !itemEntity.hasPickUpDelay() && mob.wantsToPickUp(itemEntity.getItem())) {
                    if (mob.equipItemIfPossible(itemStack)) {
                        mob.onItemPickup(itemEntity);
                        mob.take(itemEntity, itemStack.getCount());
                        itemEntity.remove();
                    }
                }
            }
        }
    }
}
