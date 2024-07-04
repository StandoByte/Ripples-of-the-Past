package com.github.standobyte.jojo.mixin.itemtracking;

import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Mixin(Slot.class)
public abstract class ContainerSlotMixin {
    @Shadow
    public IInventory container;
    
    @Inject(method = "set", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(ItemStack pStack, CallbackInfo ci) {
        if (container instanceof Entity) {
            Entity entity = (Entity) container;
            if (!entity.level.isClientSide()) {
                TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                    tracker.setAtEntity(entity.getId(), entity.level, KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> 
                            IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
                            .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                });
            }
        }
        else if (container instanceof TileEntity) {
            TileEntity tileEntity = (TileEntity) container;
            World world = tileEntity.getLevel();
            if (!world.isClientSide()) {
                TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                    tracker.setAtBlockPos(tileEntity.getBlockPos(), world, KnownItemState.BLOCK_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> 
                            IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
                            .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                });
            }
        }
        else if (container instanceof PlayerInventory) {
            PlayerEntity player = ((PlayerInventory) container).player;
            if (!player.level.isClientSide()) {
                TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                    tracker.setAtEntity(player.getId(), player.level, KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> 
                            IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
                            .anyMatch(TrackerItemStack.trackerIdCheck(trackerId)));
                });
            }
        }
    }
}
