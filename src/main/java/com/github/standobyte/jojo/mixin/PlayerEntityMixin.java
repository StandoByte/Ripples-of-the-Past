package com.github.standobyte.jojo.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void jojoPlayerUndeadCreature(CallbackInfoReturnable<CreatureAttribute> ci) {
        if (JojoModUtil.playerUndeadAttribute((LivingEntity) (Object) this)) {
            ci.setReturnValue(CreatureAttribute.UNDEAD);
        }
    }
    
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void jojoPlayerWallClimb(Vector3d pTravelVector, CallbackInfo ci) {
        PlayerEntity thisPlayer = (PlayerEntity) (Object) this;
        if (HamonWallClimbing2.travelWallClimb(thisPlayer, pTravelVector)) {
            ci.cancel();
        }
    }
    
    private static final Predicate<ItemStack> JOJO_PREFER_TRACKED_PROJECTILES = 
            item -> TrackerItemStack.getItemTracker(item).map(TrackerItemStack::isTracked).orElse(false);
    @ModifyVariable(method = "getProjectile", at = @At("STORE"), ordinal = 1)
    public ItemStack jojoPreferShootableProjectile(ItemStack ammo, ItemStack pShootable) {
        if (ammo.isEmpty() || JOJO_PREFER_TRACKED_PROJECTILES.test(ammo)) {
            return ammo;
        }
        
        PlayerEntity player = (PlayerEntity) ((Entity) this);
        Predicate<ItemStack> predicate = ((ShootableItem) pShootable.getItem()).getSupportedHeldProjectiles()
                .and(JOJO_PREFER_TRACKED_PROJECTILES);
        
        ItemStack heldTrackedAmmo = ShootableItem.getHeldProjectile(player, predicate);
        if (!heldTrackedAmmo.isEmpty()) {
            return heldTrackedAmmo;
        } else {
            predicate = ((ShootableItem) pShootable.getItem()).getAllSupportedProjectiles().and(JOJO_PREFER_TRACKED_PROJECTILES);

            for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
                ItemStack invTrackedAmmo = player.inventory.getItem(i);
                if (predicate.test(invTrackedAmmo)) {
                    return invTrackedAmmo;
                }
            }

            return ammo;
        }
    }
}
