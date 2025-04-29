package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ProjectileItemEntity.class)
public abstract class ProjectileItemEntityMixin extends ThrowableEntity {

    protected ProjectileItemEntityMixin(EntityType<? extends ThrowableEntity> p_i48540_1_, World p_i48540_2_) {
        super(p_i48540_1_, p_i48540_2_);
    }

    @Inject(method = "setItem", at = @At("HEAD"))
    public void onSetItem(ItemStack item, CallbackInfo ci) {
        if (!level.isClientSide()) {
            TrackerItemStack.getItemTracker(item).ifPresent(tracker -> {
                tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_IS_ITEM);
                tracker.setItemStillThereCheck(null);
            });
        }
    }
}
