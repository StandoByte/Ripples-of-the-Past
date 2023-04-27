package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class KnifeLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
    private final EntityRendererManager dispatcher;
    private KnifeEntity knife;

    public KnifeLayer(LivingRenderer<T, M> renderer) {
        super(renderer);
        this.dispatcher = renderer.getDispatcher();
    }

    @Override
    protected int numStuck(T entity) {
        return entity.getCapability(PlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getKnivesCount()).orElse(0);
    }

    protected void renderStuckItem(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            Entity entity, float x, float y, float z, float partialTick) {
        float f = MathHelper.sqrt(x * x + z * z);
        knife = new KnifeEntity(entity.level, entity.getX(), entity.getY(), entity.getZ());
        knife.yRot = (float)(Math.atan2((double)x, (double)z) * (double)(180F / (float)Math.PI));
        knife.xRot = (float)(Math.atan2((double)y, (double)f) * (double)(180F / (float)Math.PI));
        knife.yRotO = knife.yRot;
        knife.xRotO = knife.xRot;
        dispatcher.render(knife, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, matrixStack, buffer, packedLight);
    }
}
