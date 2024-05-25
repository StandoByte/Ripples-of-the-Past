package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements IInventory {
    @Shadow
    public PlayerEntity player;
    @Shadow
    private List<NonNullList<ItemStack>> compartments;
    
    @Inject(method = "add(ILnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"))
    public void jojoOnItemAddedToInv(int slot, ItemStack item, CallbackInfoReturnable<Boolean> ci) {
        if (!player.level.isClientSide() && Boolean.TRUE.equals(ci.getReturnValue())) {
            TrackerItemStack.getItemTrackerInInventory(item, compartments.stream().flatMap(Collection::stream))
            .ifPresent(tracker -> {
                tracker.setAtEntity(player.getId(), player.level);
                tracker.setItemStillThereCheck(null);
            });
        }
    }
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        if (!player.level.isClientSide()) {
            TrackerItemStack.getItemTracker(item)
            .ifPresent(tracker -> {
                tracker.setAtEntity(player.getId(), player.level);
                tracker.setItemStillThereCheck(null);
            });
        }
    }
}
