package com.github.standobyte.jojo.mixin.itemtracking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

@Mixin(Slot.class)
public abstract class ContainerSlotMixin {
    @Shadow
    public IInventory container;
    
    @Inject(method = "set", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(ItemStack pStack, CallbackInfo ci) {
        if (container instanceof Entity) {
            Entity entity = (Entity) container;
            TrackerItemStack.updateItemAtEntity(entity.level, pStack, entity.getId());
        }
        else if (container instanceof TileEntity) {
            TileEntity tileEntity = (TileEntity) container;
            TrackerItemStack.updateItemAtBlock(tileEntity.getLevel(), pStack, tileEntity.getBlockPos());
        }
        else if (container instanceof PlayerInventory) {
            PlayerEntity player = ((PlayerInventory) container).player;
            TrackerItemStack.updateItemAtEntity(player.level, pStack, player.getId());
        }
    }
}
