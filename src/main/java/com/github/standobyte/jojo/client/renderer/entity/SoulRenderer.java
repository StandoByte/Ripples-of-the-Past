package com.github.standobyte.jojo.client.renderer.entity;

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
                LivingRenderer renderer = (LivingRenderer) entityRenderDispatcher.getRenderer(originEntity);
                renderLiving(originEntity, renderer, renderer.getModel(), soulEntity, 
                        yRotation, partialTick, matrixStack, buffer, packedLight);
//                renderer.render(originEntity, yRotation, partialTick, matrixStack, buffer, packedLight);
            }
            super.render(soulEntity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }

    private <E extends LivingEntity, M extends EntityModel<E>> void renderLiving(E entity, LivingRenderer<E, M> renderer, M model, T soulEntity, 
            float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<E, M>(entity, renderer, partialTick, matrixStack, buffer, packedLight))) return;
        matrixStack.pushPose();
        model.attackTime = 0;

        model.riding = false;
        model.young = entity.isBaby();
        // FIXME (soul) soul entity rotation
        float yBodyRotation = MathHelper.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yHeadRotation = MathHelper.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float f2 = yHeadRotation - yBodyRotation;
        
        float xRotation = MathHelper.lerp(partialTick, entity.xRotO, entity.xRot);
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
            float alpha = Math.min(0.75F, 3.0F * (1F - Math.min((float) soulEntity.tickCount / (float) soulEntity.getLifeSpan(), 1F)));
            model.renderToBuffer(matrixStack, ivertexbuilder, packedLight, i, 1.0F, 1.0F, 0.0F, alpha);
        }

        matrixStack.popPose();
        // FIXME (soul) name tag
//        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<E, M>(entity, renderer, partialTick, matrixStack, buffer, packedLight));
    }

//    private <E extends LivingEntity, M extends EntityModel<E>> void render(E entity, LivingRenderer<E, M> renderer, M model, 
//            float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
//        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<E, M>(entity, renderer, partialTick, matrixStack, buffer, packedLight))) return;
//        matrixStack.pushPose();
//        model.attackTime = renderer.getAttackAnim(entity, partialTick);
//
//        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
//        model.riding = shouldSit;
//        model.young = entity.isBaby();
//        float yBodyRotation = MathHelper.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
//        float yHeadRotation = MathHelper.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
//        float f2 = yHeadRotation - yBodyRotation;
//        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
//            LivingEntity livingentity = (LivingEntity)entity.getVehicle();
//            yBodyRotation = MathHelper.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
//            f2 = yHeadRotation - yBodyRotation;
//            float f3 = MathHelper.wrapDegrees(f2);
//            if (f3 < -85.0F) {
//                f3 = -85.0F;
//            }
//
//            if (f3 >= 85.0F) {
//                f3 = 85.0F;
//            }
//
//            yBodyRotation = yHeadRotation - f3;
//            if (f3 * f3 > 2500.0F) {
//                yBodyRotation += f3 * 0.2F;
//            }
//
//            f2 = yHeadRotation - yBodyRotation;
//        }
//
//        float xRotation = MathHelper.lerp(partialTick, entity.xRotO, entity.xRot);
//        if (entity.getPose() == Pose.SLEEPING) {
//            Direction direction = entity.getBedOrientation();
//            if (direction != null) {
//                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
//                matrixStack.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
//            }
//        }
//
//        float ticks = renderer.getBob(entity, partialTick);
//        renderer.setupRotations(entity, matrixStack, ticks, yBodyRotation, partialTick);
//        matrixStack.scale(-1.0F, -1.0F, 1.0F);
//        renderer.scale(entity, matrixStack, partialTick);
//        matrixStack.translate(0.0D, (double)-1.501F, 0.0D);
//        float walkAnimSpeed = 0.0F;
//        float walkAnimPos = 0.0F;
//        if (!shouldSit && entity.isAlive()) {
//            walkAnimSpeed = MathHelper.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
//            walkAnimPos = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
//            if (entity.isBaby()) {
//                walkAnimPos *= 3.0F;
//            }
//
//            if (walkAnimSpeed > 1.0F) {
//                walkAnimSpeed = 1.0F;
//            }
//        }
//
//        model.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
//        model.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, f2, xRotation);
//        Minecraft minecraft = Minecraft.getInstance();
//        boolean flag = renderer.isBodyVisible(entity);
//        boolean flag1 = !flag && !entity.isInvisibleTo(minecraft.player);
//        boolean flag2 = minecraft.shouldEntityAppearGlowing(entity);
//        RenderType rendertype = renderer.getRenderType(entity, flag, flag1, flag2);
//        if (rendertype != null) {
//            IVertexBuilder ivertexbuilder = buffer.getBuffer(rendertype);
//            int i = LivingRenderer.getOverlayCoords(entity, renderer.getWhiteOverlayProgress(entity, partialTick));
//            model.renderToBuffer(matrixStack, ivertexbuilder, packedLight, i, 1.0F, 1.0F, 1.0F, 0.5F);
//        }
//
//        if (!entity.isSpectator()) {
//            for(LayerRenderer<E, M> layerrenderer : renderer.layers) {
//                layerrenderer.render(matrixStack, buffer, packedLight, entity, walkAnimPos, walkAnimSpeed, partialTick, ticks, f2, xRotation);
//            }
//        }
//
//        matrixStack.popPose();
//        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
//        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<E, M>(entity, renderer, partialTick, matrixStack, buffer, packedLight));
//    }

}
