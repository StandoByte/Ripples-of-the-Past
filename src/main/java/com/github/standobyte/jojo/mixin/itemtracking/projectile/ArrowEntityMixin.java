package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.itemtracking.ITrackedArrowEntity;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStackProvider;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.world.World;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin extends AbstractArrowEntity implements ITrackedArrowEntity {
    @Nullable private INBT itemTrackerNBT;

    protected ArrowEntityMixin(EntityType<? extends AbstractArrowEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void saveItemTrackerNBT(INBT nbt) {
        this.itemTrackerNBT = nbt;
    }
    
    @Inject(method = "getPickupItem", at = @At("RETURN"))
    public void jojoTrackPickedUpArrow(CallbackInfoReturnable<ItemStack> ci) {
        if (this.itemTrackerNBT != null) {
            ItemStack item = ci.getReturnValue();
            if (!item.isEmpty()) {
                item.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(tracker -> tracker.fromNBT(itemTrackerNBT));
            }
        }
    }

}
