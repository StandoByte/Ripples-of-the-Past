package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World level) {
        super(type, level);
    }

    @Inject(method = "getMobType", at = @At("HEAD"), cancellable = true)
    public void jojoPlayerUndeadCreature(CallbackInfoReturnable<CreatureAttribute> ci) {}
    
    
    @Shadow
    protected abstract void hurtArmor(DamageSource damageSource, float damageAmount);

    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hurtArmor(Lnet/minecraft/util/DamageSource;F)V"))
    public void jojoBarrageLessArmorBreaking(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        if (!(damageSource instanceof IModdedDamageSource && ((IModdedDamageSource) damageSource).preventsDamagingArmor())) {
            hurtArmor(damageSource, damageAmount);
        }
    }
    
    
    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    public void jojoOnClimbableFlag(CallbackInfoReturnable<Boolean> ci) {
        if (!this.isSpectator() && this.getCapability(LivingUtilCapProvider.CAPABILITY)
                .map(cap -> cap.isHamonWallClimbing()).orElse(false)) {
            ci.setReturnValue(true);
        }
    }

}
