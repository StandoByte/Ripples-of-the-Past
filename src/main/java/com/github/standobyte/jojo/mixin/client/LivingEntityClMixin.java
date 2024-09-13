package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.util.mc.MCUtil.EntityEvents;
import com.github.standobyte.jojo.util.mc.damage.NoKnockbackOnBlocking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClMixin extends Entity {

    public LivingEntityClMixin(EntityType<?> type, World level) {
        super(type, level);
    }
    
    
    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    public void jojoOnEntityEvent(byte eventId, CallbackInfo ci) {
        switch (eventId) {
        case EntityEvents.HURT:
        case EntityEvents.HURT_THORNS:
        case EntityEvents.HURT_DROWN:
        case EntityEvents.HURT_ON_FIRE:
        case EntityEvents.HURT_SWEET_BERRY_BUSH:
            if (this.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.isDyingBody()).orElse(false)) {
                ci.cancel();
            }
            break;
        }
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
