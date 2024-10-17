package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingRenderer.class)
public interface LivingRendererInvoker<T extends LivingEntity, M extends EntityModel<T>> {

    @Invoker
    void invokeScale(T entity, MatrixStack matrixStack, float partialTick);
}
