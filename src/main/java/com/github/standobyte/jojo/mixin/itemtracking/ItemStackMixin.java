package com.github.standobyte.jojo.mixin.itemtracking;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.CapabilityProvider;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin extends CapabilityProvider<ItemStack> {
    
    protected ItemStackMixin(Class<ItemStack> baseClass) {
        super(baseClass);
    }

    @Shadow private Entity entityRepresentation;
    
    @Inject(method = "setEntityRepresentation", at = @At("HEAD"))
    public void onSetEntityRepresentation(Entity entity, CallbackInfo ci) {
        if (entity != null) {
            boolean serverSide = entity != null && !entity.level.isClientSide() || entityRepresentation != null && !entityRepresentation.level.isClientSide();
            if (serverSide) {
                ItemStack asItem = (ItemStack) (Object) this;
                TrackerItemStack.getItemTracker(asItem).ifPresent(tracker -> {
                    tracker.setAtEntity(entity.getId(), entity.level, entity instanceof ItemEntity ? KnownItemState.ENTITY_IS_ITEM : KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(trackerId -> this.entityRepresentation == entity);
                });
            }
        }
    }
    
    @Inject(method = "copy", at = @At("TAIL"))
    public void onCopy(CallbackInfoReturnable<ItemStack> ci) {
        ItemStack newItem = ci.getReturnValue();
        TrackerItemStack.getItemTracker(newItem).ifPresent(tracker -> {
            ItemStack oldItem = (ItemStack) (Object) this;
            TrackerItemStack.getItemTracker(oldItem, true).ifPresent(oldTracker -> {
                tracker.copy(oldTracker);
            });
        });
    }
    
    @Inject(method = "setTag", at = @At("TAIL"))
    public void onSetTag(@Nullable CompoundNBT tag, CallbackInfo ci) {
        if (TrackerItemStack.deserializesForgeCaps(tag)) {
            deserializeCaps(tag.getCompound("ForgeCaps"));
        }
    }
}
