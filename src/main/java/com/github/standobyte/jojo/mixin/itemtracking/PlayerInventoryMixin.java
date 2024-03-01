package com.github.standobyte.jojo.mixin.itemtracking;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStackProvider;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.server.ServerWorld;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements IInventory {
    @Shadow
    public PlayerEntity player;
    @Shadow
    private List<NonNullList<ItemStack>> compartments;
    
    @Inject(method = "add(ILnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"))
    public void jojoOnItemAddedToInv(int slot, ItemStack item, CallbackInfoReturnable<Boolean> ci) {
        if (!player.level.isClientSide() && Boolean.TRUE.equals(ci.getReturnValue())) {
            item.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(oldItemTracker -> {
                if (oldItemTracker.isTracked()) {
                    UUID trackerId = oldItemTracker.getTrackerId();
                    compartments.stream().flatMap(Collection::stream).anyMatch(movedItem -> {
                        return movedItem.getCapability(TrackerItemStackProvider.CAPABILITY).map(tracker -> {
                            if (trackerId.equals(tracker.getTrackerId())) {
                                tracker.setAtEntity(player.getId());
                                tracker.onUpdate((ServerWorld) player.level);
                                return true;
                            }
                            return false;
                        }).orElse(false);
                    });
                }
            });
        }
    }
}
