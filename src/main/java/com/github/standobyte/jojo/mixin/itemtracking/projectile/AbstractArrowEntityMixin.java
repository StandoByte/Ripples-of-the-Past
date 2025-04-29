package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;

@Mixin(AbstractArrowEntity.class)
public abstract class AbstractArrowEntityMixin extends ProjectileEntity {

    public AbstractArrowEntityMixin(EntityType<? extends ProjectileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setArrowCount(I)V"))
    public void jojoOnArrowStuck(EntityRayTraceResult pResult, CallbackInfo ci) {
        SidedItemTrackerMap.getSidedTrackers(level).values().stream()
        .filter(tracker -> tracker.getAtEntity(level) == this)
        .forEach(tracker -> {
            tracker.setAtEntity(pResult.getEntity().getId(), level, KnownItemState.STUCK_ARROW);
        });
    }
}
