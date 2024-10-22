package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.github.standobyte.jojo.itemtracking.ITrackedArrowEntity;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.world.World;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ShootableItem {

    public BowItemMixin(Properties properties) {
        super(properties);
    }
    
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", 
                target = "Lnet/minecraft/entity/projectile/AbstractArrowEntity;shootFromRotation(Lnet/minecraft/entity/Entity;FFFFF)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    public void jojoModifyBowArrow(ItemStack bowItem, World world, LivingEntity entity, int holdTimeLeft, CallbackInfo ci, 
            PlayerEntity player, boolean hasAmmo, ItemStack projectileItem, int charge, float arrowPower, boolean infinity, 
            ArrowItem arrowitem, AbstractArrowEntity arrowEntity) {
        TrackerItemStack.getItemTracker(projectileItem).ifPresent(tracker -> {
            if (tracker.isTracked()) {
                tracker.setAtEntity(arrowEntity.getId(), world, KnownItemState.ENTITY_IS_ITEM);
                tracker.setItemStillThereCheck(null);
                if (arrowEntity instanceof ITrackedArrowEntity) {
                    ((ITrackedArrowEntity) arrowEntity).saveItemTrackerNBT(tracker.toNBT());
                }
            }
        });
    }
}
