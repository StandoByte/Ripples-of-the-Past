package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorBreakMixin extends Entity {

    public LivingEntityArmorBreakMixin(EntityType<?> type, World level) {
        super(type, level);
    }

    @Shadow
    protected abstract void hurtArmor(DamageSource damageSource, float damageAmount);

    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hurtArmor(Lnet/minecraft/util/DamageSource;F)V"))
    public void jojoBarrageLessArmorBreaking(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        if (!(damageSource instanceof IModdedDamageSource && ((IModdedDamageSource) damageSource).preventsDamagingArmor())) {
            hurtArmor(damageSource, damageAmount);
        }
    }

}
