package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClMixin extends Entity {

    public LivingEntityClMixin(EntityType<?> type, World level) {
        super(type, level);
    }
    
    
    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    public void jojoOnEntityEvent(byte eventId, CallbackInfo ci) {
        switch (eventId) {
        case 2:
        case 33:
        case 36:
        case 37:
        case 44:
            if (this.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.isDyingBody()).orElse(false)) {
                ci.cancel();
            }
            break;
        }
    }
}
