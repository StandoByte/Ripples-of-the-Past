package com.github.standobyte.jojo.itemtracking;

//@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ItemTrackingEventHandler {
    
    // was replaced by ItemStackMixin#onSetEntityRepresentation
//    @SubscribeEvent
//    public static void trackItemInItemEntity(EntityJoinWorldEvent event) {
//        Entity entity = event.getEntity();
//        if (!entity.level.isClientSide() && entity instanceof ItemEntity) {
//            ItemEntity itemEntity = ((ItemEntity) entity);
//            ItemStack item = itemEntity.getItem();
//            TrackerItemStack.getItemTracker(item).ifPresent(tracker -> {
//                tracker.setAtEntity(entity.getId(), entity.level, KnownItemState.ENTITY_IS_ITEM);
//                tracker.setItemStillThereCheck(trackerId -> TrackerItemStack.trackerIdCheck(trackerId).test(itemEntity.getItem()));
//            });
//        }
//    }
    
}
