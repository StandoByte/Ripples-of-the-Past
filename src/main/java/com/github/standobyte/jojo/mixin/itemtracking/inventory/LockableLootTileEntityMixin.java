package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

@Mixin(LockableLootTileEntity.class)
public abstract class LockableLootTileEntityMixin extends LockableTileEntity {
    
    protected LockableLootTileEntityMixin(TileEntityType<?> type) {
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
                tracker.setAtBlockPos(this.getBlockPos(), level);
                tracker.setItemStillThereCheck(null);
            });
        }
    }
}
