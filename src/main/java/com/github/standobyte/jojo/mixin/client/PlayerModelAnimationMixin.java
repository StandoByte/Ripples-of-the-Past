package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler.BendablePart;
import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(value = PlayerModel.class, priority = 3000) // and this... is to go even further beyond!
public abstract class PlayerModelAnimationMixin<T extends LivingEntity> extends BipedModel<T> {
    
    public PlayerModelAnimationMixin(float p_i1148_1_) {
        super(p_i1148_1_);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/model/ModelRenderer;copyFrom(Lnet/minecraft/client/renderer/model/ModelRenderer;)V", ordinal = 0))
    public void jojoPlayerModelPoseAfterPlayerAnimator(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null && vehicle.getType() == ModEntityTypes.LEAVES_GLIDER.get()) {
            leftArm.xRot = (float)Math.PI;
            leftArm.yRot = 0;
            leftArm.zRot = 0;
            rightArm.xRot = (float)Math.PI;
            rightArm.yRot = 0;
            rightArm.zRot = 0;
            body.zRot = 0;
            PlayerAnimationHandler.getPlayerAnimator().setBend(this, BendablePart.LEFT_ARM, 0, 0);
            PlayerAnimationHandler.getPlayerAnimator().setBend(this, BendablePart.RIGHT_ARM, 0, 0);
        }
    }
    
}
