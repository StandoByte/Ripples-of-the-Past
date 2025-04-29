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
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ShootableItem {

    public CrossbowItemMixin(Properties properties) {
        super(properties);
    }
    
    @Inject(method = "shootProjectile", at = @At(value = "INVOKE", 
                target = "Lnet/minecraft/entity/projectile/ProjectileEntity;shoot(DDDFF)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void jojoModifyCrossbowArrow(World pLevel, LivingEntity pShooter, Hand arg2, 
            ItemStack pCrossbowStack, ItemStack pAmmoStack, float pSoundPitch, boolean pIsCreativeMode, 
            float pVelocity, float pInaccuracy, float pProjectileAngle, CallbackInfo ci, 
            boolean firework, ProjectileEntity projectileEntity) {
        if (pProjectileAngle == 0) { // in case of multishot
            TrackerItemStack.getItemTracker(pAmmoStack).ifPresent(tracker -> {
                if (tracker.isTracked()) {
                    tracker.setAtEntity(projectileEntity.getId(), pLevel, KnownItemState.ENTITY_IS_ITEM);
                    tracker.setItemStillThereCheck(null);
                    if (projectileEntity instanceof ITrackedArrowEntity) {
                        ((ITrackedArrowEntity) projectileEntity).saveItemTrackerNBT(tracker.toNBT());
                    }
                }
            });
        }
    }
}
