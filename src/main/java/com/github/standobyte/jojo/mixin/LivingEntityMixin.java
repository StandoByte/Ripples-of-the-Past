package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mod.NoKnockbackOnBlocking;

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
        if (!this.isSpectator() && LivingWallClimbing.getHandler((LivingEntity) (Entity) this).map(cap -> cap.isWallClimbing()).orElse(false)) {
            ci.setReturnValue(true);
        }
    }
    
    
    @Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
    public void jojoCancelHurtSound(DamageSource pSource, CallbackInfo ci) {
        if (NoKnockbackOnBlocking.cancelHurtSound((LivingEntity) (Entity) this)) {
            ci.cancel();
        }
    }

}
