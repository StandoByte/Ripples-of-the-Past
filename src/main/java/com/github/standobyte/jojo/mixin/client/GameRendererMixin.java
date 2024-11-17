package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.LivingEntity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    @ModifyVariable(method = "bobHurt", remap = false, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float jojoTsBobHurtChangePartialTick(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) mc.getCameraEntity();
            if (entity.getCapability(EntityUtilCapProvider.CAPABILITY).map(EntityUtilCap::wasStoppedInTime).orElse(false)) {
                return 1.0F;
            }
        }
        return partialTick;
    }
    
}
