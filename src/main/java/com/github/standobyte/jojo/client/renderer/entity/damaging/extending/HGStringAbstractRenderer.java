package com.github.standobyte.jojo.client.renderer.entity.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.model.entity.ownerbound.repeating.HGStringModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public abstract class HGStringAbstractRenderer<T extends OwnerBoundProjectileEntity> extends ExtendingEntityRenderer<T, HGStringModel<T>> {
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hg_string_glow.png");

    public HGStringAbstractRenderer(EntityRendererManager renderManager, HGStringModel<T> model) {
        super(renderManager, model, new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hg_string.png"));
    }
    
    @Override
    protected void doRender(T entity, HGStringModel<T> model, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        super.doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(model.renderType(GLOW_TEXTURE)), ClientUtil.MAX_MODEL_LIGHT);
    }
}
