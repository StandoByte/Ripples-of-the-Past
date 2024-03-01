package com.github.standobyte.jojo.mixin.itemtracking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;

@Mixin(HopperTileEntity.class)
public abstract class HopperTileEntityMixin extends LockableLootTileEntity {
    
    protected HopperTileEntityMixin(TileEntityType<?> type) {
        super(type);
    }
    
    @Shadow
    protected abstract NonNullList<ItemStack> getItems();
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        TrackerItemStack.trackedInBlockInv(item, getItems().stream(), 
                level, getBlockPos());
    }
}
