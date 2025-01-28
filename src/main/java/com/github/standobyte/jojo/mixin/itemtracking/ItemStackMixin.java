package com.github.standobyte.jojo.mixin.itemtracking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow private Entity entityRepresentation;
    
    @Inject(method = "setEntityRepresentation", at = @At("HEAD"))
    public void onSetEntityRepresentation(Entity entity, CallbackInfo ci) {
        if (entity != null) {
            boolean serverSide = entity != null && !entity.level.isClientSide() || entityRepresentation != null && !entityRepresentation.level.isClientSide();
            if (serverSide) {
                ItemStack asItem = (ItemStack) (Object) this;
                TrackerItemStack.getItemTracker(asItem).ifPresent(tracker -> {
                    tracker.setAtEntity(entity.getId(), entity.level, entity instanceof ItemEntity ? KnownItemState.ENTITY_IS_ITEM : KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> this.entityRepresentation == entity);
                });
            }
        }
    }
}
