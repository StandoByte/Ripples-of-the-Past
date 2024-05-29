package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.LivingEntity;
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
    
    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/item/ArrowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/entity/projectile/AbstractArrowEntity;"))
    public AbstractArrowEntity jojoModifyBowArrow(ArrowItem arrowItem, World world, ItemStack arrowItemStack, LivingEntity shooter) {
        AbstractArrowEntity arrowEntity = arrowItem.createArrow(world, arrowItemStack, shooter);
        TrackerItemStack.getItemTracker(arrowItemStack).ifPresent(tracker -> {
            tracker.setAtEntity(arrowEntity.getId(), world);
            tracker.setItemStillThereCheck(null);
        });
        return arrowEntity;
    }
}
