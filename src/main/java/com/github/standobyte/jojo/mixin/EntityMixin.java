package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.entity.IPassengerMixinReposition;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "Lnet/minecraft/entity/Entity;positionRider("
            + "Lnet/minecraft/entity/Entity;"
            + "Lnet/minecraft/entity/Entity$IMoveCallback;)V", at = @At("TAIL"))
    public void jojoRepositionPassenger(Entity passenger, Entity.IMoveCallback moveMethod, CallbackInfo ci) {
        Entity thisAsEntity = (Entity) (Object) this;
        if (passenger instanceof IPassengerMixinReposition && thisAsEntity.hasPassenger(passenger)) {
            Vector3d passengerPosition = ((IPassengerMixinReposition) passenger).repositionPassenger(thisAsEntity);
            if (passengerPosition != null) {
                moveMethod.accept(passenger, passengerPosition.x, passengerPosition.y, passengerPosition.z);
            }
        }
    }
}
