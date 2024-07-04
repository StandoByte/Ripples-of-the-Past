package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.client.entity.ClientConsciousnessEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

public class ConsciousnessRenderer extends EntityRenderer<ClientConsciousnessEntity> {

    public ConsciousnessRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(ClientConsciousnessEntity p_110775_1_) {
        return null;
    }

    @SuppressWarnings("resource")
    @Override
    public void render(ClientConsciousnessEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        ClientConsciousnessEntity csnsEntity = (ClientConsciousnessEntity) entity;
//        partialTick = ControllerConsciousness.getInstance().getConsciousnessPartialTick(partialTick);
        renderCsns(Minecraft.getInstance().player, csnsEntity, 
                yRotation, partialTick, matrixStack, buffer, packedLight);
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }
    
    private void renderCsns(AbstractClientPlayerEntity player, ClientConsciousnessEntity csnsEntity, 
            float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        PlayerRenderer renderer = (PlayerRenderer) entityRenderDispatcher.getRenderer(player);
        if (MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Pre(player, renderer, partialTick, matrixStack, buffer, packedLight))) return;
        
        PlayerModel<AbstractClientPlayerEntity> model = renderer.getModel();
        
        matrixStack.pushPose();
        model.attackTime = csnsEntity.getAttackAnim(partialTick);

        boolean shouldSit = csnsEntity.isPassenger() && (csnsEntity.getVehicle() != null && csnsEntity.getVehicle().shouldRiderSit());
        model.riding = shouldSit;
        model.young = player.isBaby();
        float f = MathHelper.rotLerp(partialTick, csnsEntity.yBodyRotO, csnsEntity.yBodyRot);
        float f1 = MathHelper.rotLerp(partialTick, csnsEntity.yHeadRotO, csnsEntity.yHeadRot);
        float f2 = f1 - f;
        if (shouldSit && csnsEntity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)csnsEntity.getVehicle();
            f = MathHelper.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
            f2 = f1 - f;
            float f3 = MathHelper.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            f2 = f1 - f;
        }

        float f6 = MathHelper.lerp(partialTick, csnsEntity.xRotO, csnsEntity.xRot);
        if (csnsEntity.getPose() == Pose.SLEEPING) {
            Direction direction = csnsEntity.getBedOrientation();
            if (direction != null) {
                float f4 = csnsEntity.getEyeHeight(Pose.STANDING) - 0.1F;
                matrixStack.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
            }
        }

        float f7 = csnsEntity.tickCount + partialTick;
        setupRotations(csnsEntity, matrixStack, f7, f, partialTick);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
        matrixStack.translate(0.0D, (double)-1.501F, 0.0D);
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (!shouldSit && csnsEntity.isAlive()) {
            f8 = MathHelper.lerp(partialTick, csnsEntity.animationSpeedOld, csnsEntity.animationSpeed);
            f5 = csnsEntity.animationPosition - csnsEntity.animationSpeed * (1.0F - partialTick);
            if (player.isBaby()) {
                f5 *= 3.0F;
            }

            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        model.prepareMobModel(csnsEntity, f5, f8, partialTick);
        model.setupAnim(csnsEntity, f5, f8, f7, f2, f6);
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = !csnsEntity.isInvisible();
        boolean flag1 = !flag && !csnsEntity.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(csnsEntity);
        RenderType rendertype = RenderType.itemEntityTranslucentCull(renderer.getTextureLocation(player));
        if (rendertype != null) {
            IVertexBuilder ivertexbuilder = buffer.getBuffer(rendertype);
            int i = OverlayTexture.pack(OverlayTexture.u(0.5F), OverlayTexture.v(false));
            model.renderToBuffer(matrixStack, ivertexbuilder, packedLight, i, 1.0F, 1.0F, 1.0F, 1.0F);
        }

//        if (!pEntity.isSpectator()) {
//            for(LayerRenderer<T, M> layerrenderer : this.layers) {
//                layerrenderer.render(matrixStack, pBuffer, pPackedLight, pEntity, f5, f8, partialTick, f7, f2, f6);
//            }
//        }

        matrixStack.popPose();
        
        MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Post(player, renderer, partialTick, matrixStack, buffer, packedLight));
    }
    


    protected void setupRotations(PlayerEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        float f = pEntityLiving.getSwimAmount(pPartialTicks);
        if (pEntityLiving.isFallFlying()) {
            setupRotations2(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
            float f1 = (float)pEntityLiving.getFallFlyingTicks() + pPartialTicks;
            float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!pEntityLiving.isAutoSpinAttack()) {
                pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - pEntityLiving.xRot)));
            }

            Vector3d vector3d = pEntityLiving.getViewVector(pPartialTicks);
            Vector3d vector3d1 = pEntityLiving.getDeltaMovement();
            double d0 = Entity.getHorizontalDistanceSqr(vector3d1);
            double d1 = Entity.getHorizontalDistanceSqr(vector3d);
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                pMatrixStack.mulPose(Vector3f.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            setupRotations2(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
            float f3 = pEntityLiving.isInWater() ? -90.0F - pEntityLiving.xRot : -90.0F;
            float f4 = MathHelper.lerp(f, 0.0F, f3);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f4));
            if (pEntityLiving.isVisuallySwimming()) {
                pMatrixStack.translate(0.0D, -1.0D, (double)0.3F);
            }
        } else {
            setupRotations2(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        }
    }
    
    protected void setupRotations2(PlayerEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        Pose pose = pEntityLiving.getPose();
        if (pose != Pose.SLEEPING) {
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
        }

        if (pEntityLiving.deathTime > 0) {
            float f = ((float)pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 90));
        } else if (pEntityLiving.isAutoSpinAttack()) {
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - pEntityLiving.xRot));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(((float)pEntityLiving.tickCount + pPartialTicks) * -75.0F));
        } else if (pose == Pose.SLEEPING) {
            Direction direction = pEntityLiving.getBedOrientation();
            float f1 = direction != null ? sleepDirectionToRotation(direction) : pRotationYaw;
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
        } else if (pEntityLiving.hasCustomName() || pEntityLiving instanceof PlayerEntity) {
            String s = TextFormatting.stripFormatting(pEntityLiving.getName().getString());
            if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(pEntityLiving instanceof PlayerEntity) || ((PlayerEntity)pEntityLiving).isModelPartShown(PlayerModelPart.CAPE))) {
                pMatrixStack.translate(0.0D, (double)(pEntityLiving.getBbHeight() + 0.1F), 0.0D);
                pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            }
        }
    }
    
    private static float sleepDirectionToRotation(Direction pFacing) {
        switch(pFacing) {
        case SOUTH:
            return 90.0F;
        case WEST:
            return 0.0F;
        case NORTH:
            return 270.0F;
        case EAST:
            return 180.0F;
        default:
            return 0.0F;
        }
    }
}
