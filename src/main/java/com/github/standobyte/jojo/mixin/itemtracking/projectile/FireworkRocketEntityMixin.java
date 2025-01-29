package com.github.standobyte.jojo.mixin.itemtracking.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends ProjectileEntity {

    public FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> p_i231584_1_, World p_i231584_2_) {
        super(p_i231584_1_, p_i231584_2_);
    }
    
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    public void onEntityCreated(World world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        TrackerItemStack.getItemTracker(stack).ifPresent(tracker -> {
            tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_IS_ITEM);
            tracker.setItemStillThereCheck(null);
        });
    }

}
