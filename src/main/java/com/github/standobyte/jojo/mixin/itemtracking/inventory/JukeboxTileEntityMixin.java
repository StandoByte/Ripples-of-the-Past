package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;

@Mixin(JukeboxTileEntity.class)
public abstract class JukeboxTileEntityMixin extends TileEntity {
    
    public JukeboxTileEntityMixin(TileEntityType<?> teType) {
        super(teType);
    }

    @Shadow private ItemStack record;
    
    @Inject(method = "setRecord", at = @At("HEAD"))
    public void onSetRecord(ItemStack record, CallbackInfo ci) {
        World world = getLevel();
        if (world != null && !world.isClientSide()) {
            TrackerItemStack.getItemTracker(record, false)
            .ifPresent(tracker -> {
                tracker.setAtBlockPos(this.getBlockPos(), level, KnownItemState.BLOCK_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> TrackerItemStack.hasTrackerId(this.record, trackerId));
            });
        }
    }
}
