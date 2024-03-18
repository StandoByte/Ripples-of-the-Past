package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.SoulEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;

public class SoulRenderer<T extends SoulEntity> extends EntityRenderer<T> {

    public SoulRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return null;
    }

    @Override
    public void render(T soulEntity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (!soulEntity.isInvisibleTo(Minecraft.getInstance().player)) {
            LivingEntity originEntity = soulEntity.getOriginEntity();
            if (originEntity != null) {
                renderSoul(originEntity, soulEntity, 
                        yRotation, partialTick, matrixStack, buffer, packedLight);
            }
            super.render(soulEntity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    private <E extends LivingEntity, M extends EntityModel<E>> void renderSoul(E entity, T soulEntity, 
            float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        LivingRenderer<E, M> renderer = (LivingRenderer<E, M>) entityRenderDispatcher.getRenderer(entity);
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<E, M>(entity, renderer, partialTick, matrixStack, buffer, packedLight))) return;
        
        M model = renderer.getModel();
        matrixStack.pushPose();
        model.attackTime = 0;

        model.riding = false;
        model.young = entity.isBaby();
        float yHeadRotation = MathHelper.rotLerp(partialTick, soulEntity.yHeadRotO, soulEntity.yHeadRot);
        float yBodyRotation = MathHelper.rotLerp(partialTick, soulEntity.yBodyRotO, soulEntity.yBodyRot);
        float f2 = yHeadRotation - yBodyRotation;
        
        float xRotation = MathHelper.lerp(partialTick, soulEntity.xRotO, soulEntity.xRot);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - yBodyRotation));

        float ticks = entity.tickCount + partialTick;
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.translate(0.0D, (double)-1.501F, 0.0D);

        model.prepareMobModel(entity, 0, 0, partialTick);
        model.setupAnim(entity, 0, 0, ticks, f2, xRotation);
        RenderType rendertype = RenderType.itemEntityTranslucentCull(renderer.getTextureLocation(entity));
        if (rendertype != null) {
            IVertexBuilder ivertexbuilder = buffer.getBuffer(rendertype);
            int i = OverlayTexture.pack(OverlayTexture.u(0.5F), OverlayTexture.v(false));
            float alpha = Math.min(0.75F, 3.0F * (1F - Math.min((float) soulEntity.tickCount / (float) soulEntity.lifeSpan, 1F)));
            model.renderToBuffer(matrixStack, ivertexbuilder, packedLight, i, 1.0F, 1.0F, 0.0F, alpha);
        }

        matrixStack.popPose();
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<E, M>(entity, renderer, partialTick, matrixStack, buffer, packedLight));
    }
}
