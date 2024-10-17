package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements IInventory {
    @Shadow
    @Final private NonNullList<ItemStack> items;
    @Shadow
    private List<IInventoryChangedListener> listeners;
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        if (listeners != null) {
            for (IInventoryChangedListener shouldBeHorse : listeners) {
                if (shouldBeHorse instanceof AbstractHorseEntity) {
                    AbstractHorseEntity horse = (AbstractHorseEntity) shouldBeHorse;
                    if (!horse.level.isClientSide()) {
                        TrackerItemStack.getItemTrackerInInventory(item, items.stream())
                        .ifPresent(tracker -> {
                            tracker.setAtEntity(horse.getId(), horse.level, KnownItemState.ENTITY_HAS_ITEM);
                            tracker.setItemStillThereCheck(trackerId -> items.stream()
                                    .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                        });
                    }
                    break;
                }
            }
        }
    }
}
