package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

@Mixin(HopperTileEntity.class)
public abstract class HopperTileEntityMixin extends LockableLootTileEntity {
    
    protected HopperTileEntityMixin(TileEntityType<?> type) {
        super(type);
    }
    
    @Shadow
    protected abstract NonNullList<ItemStack> getItems();
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        World world = getLevel();
        if (!world.isClientSide()) {
            TrackerItemStack.getItemTrackerInInventory(item, getItems().stream())
            .ifPresent(tracker -> {
                tracker.setAtBlockPos(this.getBlockPos(), level, KnownItemState.BLOCK_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> getItems().stream()
                        .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
            });
        }
    }
}
