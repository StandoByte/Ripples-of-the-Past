package com.github.standobyte.jojo.mixin.timestop;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.timestop.EntityTimeStop;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobNoAIFlag extends LivingEntity {

    protected MobNoAIFlag(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Inject(method = "isNoAi", at = @At("HEAD"), cancellable = true)
    public void noAIInTimeStop(CallbackInfoReturnable<Boolean> ci) {
        if (((EntityTimeStop) this).jojo_ripples$isStoppedInTime()) {
            ci.setReturnValue(true);
        }
    }
}
