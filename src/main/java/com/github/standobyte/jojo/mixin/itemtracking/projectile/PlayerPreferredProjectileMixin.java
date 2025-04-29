package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerPreferredProjectileMixin extends LivingEntity {
    
    protected PlayerPreferredProjectileMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }
    
    private static final Predicate<ItemStack> JOJO_PREFER_TRACKED_PROJECTILES = 
            item -> TrackerItemStack.getItemTracker(item).map(TrackerItemStack::isTracked).orElse(false);
    @ModifyVariable(method = "getProjectile", at = @At("STORE"), ordinal = 1)
    public ItemStack jojoPreferShootableProjectile(ItemStack offHandAmmo, ItemStack pShootable) {
        if (JOJO_PREFER_TRACKED_PROJECTILES.test(offHandAmmo)) {
            return offHandAmmo;
        }
        
        PlayerEntity player = (PlayerEntity) ((LivingEntity) this);
        Predicate<ItemStack> predicate = ((ShootableItem) pShootable.getItem()).getAllSupportedProjectiles().and(JOJO_PREFER_TRACKED_PROJECTILES);
        
        for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
            ItemStack invTrackedAmmo = player.inventory.getItem(i);
            if (predicate.test(invTrackedAmmo)) {
                return invTrackedAmmo;
            }
        }
        
        return offHandAmmo;
    }
}
