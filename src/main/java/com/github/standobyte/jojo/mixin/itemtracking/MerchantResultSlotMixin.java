package com.github.standobyte.jojo.mixin.itemtracking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.inventory.container.MerchantResultSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.world.server.ServerWorld;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
    @Shadow @Final private MerchantInventory slots;
    @Shadow @Final private IMerchant merchant;

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = 
            "Lnet/minecraft/item/MerchantOffer;take(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 0))
    public void onTradePerform(PlayerEntity player, ItemStack item, CallbackInfoReturnable<ItemStack> ci) {
        if (!player.level.isClientSide()) {
            ItemStack playerOfferA = slots.getItem(0);
            ItemStack playerOfferB = slots.getItem(1);
            MerchantOffer offer = slots.getActiveOffer();
            if ((offer.satisfiedBy(playerOfferA, playerOfferB) || offer.satisfiedBy(playerOfferB, playerOfferA)) && merchant instanceof Entity) {
                Entity merchantEntity = (Entity) merchant;
                ServerWorld world = (ServerWorld) player.level;
                TrackerItemStack.getItemTracker(playerOfferA).ifPresent(tracker -> trackTradeCost(tracker, merchantEntity, world));
                TrackerItemStack.getItemTracker(playerOfferB).ifPresent(tracker -> trackTradeCost(tracker, merchantEntity, world));
            }
        }
    }
    
    private static void trackTradeCost(TrackerItemStack tracker, Entity merchantEntity, ServerWorld world) {
        ItemStack itemCopy = tracker.getItem().copy();
        tracker.moveToItem(itemCopy, world);
        TrackerItemStack.getItemTracker(itemCopy).ifPresent(newTracker -> {
            newTracker.setAtEntity(merchantEntity.getId(), merchantEntity.level, KnownItemState.ENTITY_HAS_ITEM);
            newTracker.setItemStillThereCheck(null);
        });
    }
}
