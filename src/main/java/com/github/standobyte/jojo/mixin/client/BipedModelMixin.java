package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(BipedModel.class)
public abstract class BipedModelMixin<T extends LivingEntity> extends AgeableModel<T> {
    @Shadow public ModelRenderer leftArm;
    @Shadow public ModelRenderer rightArm;
    @Shadow public ModelRenderer body;
    
    @Inject(method = "setupAnim", at = @At("TAIL"))
    public void jojoBipedModelPose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null && vehicle.getType() == ModEntityTypes.LEAVES_GLIDER.get()) {
            leftArm.xRot = (float)Math.PI;
            leftArm.yRot = 0;
            leftArm.zRot = 0;
            rightArm.xRot = (float)Math.PI;
            rightArm.yRot = 0;
            rightArm.zRot = 0;
            body.zRot = 0;
        }
    }
    
}
