package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.standobyte.jojo.util.mc.damage.NoKnockbackOnBlocking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClMixin extends Entity {

    public LivingEntityClMixin(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
    }

    @Shadow
    protected abstract SoundEvent getHurtSound(DamageSource damageSource);
    
    @Redirect(method = "handleEntityEvent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getHurtSound(Lnet/minecraft/util/DamageSource;)Lnet/minecraft/util/SoundEvent;"))
    public SoundEvent jojoCancelClientHurtSound(LivingEntity entity, DamageSource damageSource) {
        if (NoKnockbackOnBlocking.cancelHurtSound((LivingEntity) (Entity) this)) {
            return null;
        }
        
        return getHurtSound(damageSource);
    }
}
