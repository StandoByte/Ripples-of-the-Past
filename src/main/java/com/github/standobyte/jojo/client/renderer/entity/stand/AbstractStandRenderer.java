package com.github.standobyte.jojo.client.renderer.entity.stand;

import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel.VisibilityMode;
import com.github.standobyte.jojo.client.renderer.entity.stand.layer.StandModelLayerRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public abstract class AbstractStandRenderer<T extends StandEntity, M extends StandEntityModel<T>> extends LivingRenderer<T, M> {

    public AbstractStandRenderer(EntityRendererManager rendererManager, M entityModel, float shadowRadius) {
        super(rendererManager, entityModel, shadowRadius);
        entityModel.afterInit();
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return super.shouldShowName(entity) && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    protected boolean isBodyVisible(T entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
    protected boolean visibleForSpectator(T entity) {
        return entity.underInvisibilityEffect() && Minecraft.getInstance().player.isSpectator();
    }

    public RenderType getRenderType(T entity, ResourceLocation modelTexture) {
        return renderType(entity, modelTexture, isBodyVisible(entity), visibleForSpectator(entity), Minecraft.getInstance().shouldEntityAppearGlowing(entity));
    }

    protected RenderType renderType(T entity, ResourceLocation modelTexture, boolean isVisible, boolean isVisibleForSpectator, boolean isGlowing) {
        if (isVisible || isVisibleForSpectator) {
            return isGlowing ? RenderType.outline(modelTexture) : getModel().renderType(modelTexture);
        }
        return null;
    }

    @Deprecated
    @Override
    public RenderType getRenderType(T entity, boolean isVisible, boolean isVisibleForSpectator, boolean isGlowing) {
        return renderType(entity, getTextureLocation(entity), isVisible, isVisibleForSpectator, isGlowing);
    }
    
    protected float getAlpha(T entity, float partialTick) {
        if (visibleForSpectator(entity)) {
            return 0.15F;
        }
        if (entity.isFollowingUser() && !entity.isArmsOnlyMode()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.getCameraType().isFirstPerson()) {
                Entity user = entity.getUser();
                if (mc.player.is(user)) {
                    Vector3d diffVec = entity.getPosition(partialTick).subtract(user.getPosition(partialTick));
                    if (diffVec.lengthSqr() < 0.09 || diffVec.lengthSqr() < 1 && user.getViewVector(partialTick).dot(diffVec) > diffVec.lengthSqr() / 2) {
                        return 0.25F;
                    }
                }
            }
        }
        return entity.getAlpha(partialTick);
    }

    private static final float PLAYER_RENDER_SCALE = 0.9375F;
    @Override
    protected void scale(T entity, MatrixStack matrixStack, float partialTick) {
        matrixStack.scale(
                PLAYER_RENDER_SCALE * entity.getBbWidth() / 0.6F, 
                PLAYER_RENDER_SCALE * entity.getBbHeight() / 1.8F, 
                PLAYER_RENDER_SCALE * entity.getBbWidth() / 0.6F);
    }

    private static final float OVERLAY_TICKS = 10.0F;
    @Override
    protected float getWhiteOverlayProgress(StandEntity entity, float partialTick) {
        return entity.isArmsOnlyMode() || entity.overlayTickCount > OVERLAY_TICKS ? 0 : 
            (OVERLAY_TICKS - MathHelper.clamp(entity.overlayTickCount + partialTick, 0.0F, OVERLAY_TICKS)) / OVERLAY_TICKS;
    }

    @Override
    public void render(T entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<T, M>(entity, this, partialTick, matrixStack, buffer, packedLight))) return;
        matrixStack.pushPose();
        model.attackTime = this.getAttackAnim(entity, partialTick);
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        model.riding = shouldSit;
        model.young = entity.isBaby();
        model.setVisibilityMode(visibilityMode(entity), false);
        float yBodyRotation = MathHelper.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yHeadRotation = MathHelper.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float f2 = yHeadRotation - yBodyRotation;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity vehicle = (LivingEntity)entity.getVehicle();
            yBodyRotation = MathHelper.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            float f3 = MathHelper.clamp(MathHelper.wrapDegrees(yHeadRotation - yBodyRotation), -85F, 85F);
            yBodyRotation = yHeadRotation - f3;
            if (Math.abs(f3) > 50F) {
                yBodyRotation += f3 * 0.2F;
            }
            f2 = yHeadRotation - yBodyRotation;
        }

        float xRotation = MathHelper.lerp(partialTick, entity.xRotO, entity.xRot);
        if (entity.getPose() == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                matrixStack.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
            }
        }

        float ticks = getBob(entity, partialTick);
        setupRotations(entity, matrixStack, ticks, yBodyRotation, partialTick);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        scale(entity, matrixStack, partialTick);
        matrixStack.translate(0.0D, -1.501D, 0.0D);
        float walkAnimSpeed = 0.0F;
        float walkAnimPos = 0.0F;
        if (entity.isAlive()) {
            walkAnimSpeed = MathHelper.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
            walkAnimPos = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
            if (entity.isBaby()) {
                walkAnimPos *= 3.0F;
            }

            if (walkAnimSpeed > 1.0F) {
                walkAnimSpeed = 1.0F;
            }
        }

        if (!entity.isVisibleForAll() && model.getPose() == StandPose.IDLE && model.attackTime == 0 && 
                entity.isFollowingUser() && !Minecraft.getInstance().player.isShiftKeyDown()) {
            float idleY = MathHelper.sin((ticks - model.idleLoopTickStamp) * 0.04F) * 0.04F;
            matrixStack.translate(0.0D, idleY, 0.0D);
        }
        
        model.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
        model.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, f2, xRotation);
        RenderType renderType = getRenderType(entity, getTextureLocation(entity));
        if (renderType != null) {
            IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);
            int packedOverlay = getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick));
            float alpha = getAlpha(entity, partialTick);
            model.renderToBuffer(matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
            if (!entity.isSpectator()) {
                for (LayerRenderer<T, M> layerRenderer : this.layers) {
                    layerRenderer.render(matrixStack, buffer, packedLight, entity, walkAnimPos, walkAnimSpeed, partialTick, ticks, f2, xRotation);
                }
            }
            model.renderArmSwings(entity, matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
        }

        matrixStack.popPose();

        RenderNameplateEvent renderNameplateEvent = new RenderNameplateEvent(entity, entity.getDisplayName(), this, matrixStack, buffer, packedLight, partialTick);
        MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
        if (renderNameplateEvent.getResult() != Event.Result.DENY && (renderNameplateEvent.getResult() == Event.Result.ALLOW || this.shouldShowName(entity))) {
            this.renderNameTag(entity, renderNameplateEvent.getContent(), matrixStack, buffer, packedLight);
        }

        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(entity, this, partialTick, matrixStack, buffer, packedLight));
    }

    public void renderLayer(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight,
            T entity, float walkAnimSpeed, float walkAnimPos, float partialTick,
            float ticks, float yRotationOffset, float xRotation, 
            ResourceLocation layerModelTexture, M model) {
        getModel().copyPropertiesTo(model);
        model.setVisibilityMode(visibilityMode(entity), false);
        model.prepareMobModel(entity, walkAnimSpeed, walkAnimPos, partialTick);
        model.setupAnim(entity, walkAnimSpeed, walkAnimPos, ticks, yRotationOffset, xRotation);
        int packedOverlay = getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick));
        float alpha = getAlpha(entity, partialTick);
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
        model.renderArmSwings(entity, matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
    }

    private VisibilityMode visibilityMode(T entity) {
        if (!entity.isArmsOnlyMode()) {
            return VisibilityMode.ALL;
        }
        boolean mainArm = entity.showArm(Hand.MAIN_HAND);
        boolean offArm = entity.showArm(Hand.OFF_HAND);
        if (mainArm && offArm) {
            return VisibilityMode.ARMS_ONLY;
        }
        HandSide hand = offArm ? entity.getMainArm().getOpposite() : entity.getMainArm();
        switch (hand) {
        case RIGHT:
            return VisibilityMode.RIGHT_ARM_ONLY;
        default:
            return VisibilityMode.LEFT_ARM_ONLY;
        }
    }
    

    public void renderFirstPersonArm(HandSide handSide, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, float partialTick) {
        RenderType renderType = getRenderType(entity, getTextureLocation(entity));
        if (renderType != null) {
            renderSeparateLayerArm(getModel(), handSide, matrixStack, buffer.getBuffer(renderType), packedLight, entity, partialTick);
            for (LayerRenderer<?, ?> layer : layers) {
                if (layer instanceof StandModelLayerRenderer) {
                    StandModelLayerRenderer<T, M> standLayer = (StandModelLayerRenderer<T, M>) layer;
                    if (standLayer.shouldRender(entity)) {
                        renderSeparateLayerArm(standLayer.getLayerModel(), handSide, matrixStack, 
                                standLayer.getBuffer(buffer, entity), standLayer.getPackedLight(packedLight), entity, partialTick);
                    }
                }
            }
        }
    }

    private void renderSeparateLayerArm(M model, HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder vertexBuilder, int packedLight, T entity, float partialTick) {
        matrixStack.pushPose();
        model.attackTime = this.getAttackAnim(entity, partialTick);
        model.young = entity.isBaby();
        float walkAnimSpeed = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
        if (entity.isBaby()) {
            walkAnimSpeed *= 3.0F;
        }
        float walkAnimPos = Math.min(MathHelper.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed), 1.0F);
        float ticks = getBob(entity, partialTick);
        float yBodyRotation = MathHelper.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yHeadRotation = MathHelper.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float yRotationOffset = yHeadRotation - yBodyRotation;
        matrixStack.translate(0, 0.75F, 0);
        model.prepareMobModel(entity, walkAnimSpeed, walkAnimPos, partialTick); 
        model.setupAnim(entity, walkAnimSpeed, walkAnimPos, ticks, yRotationOffset, 0);
        if (model.attackTime > 0 || entity.getStandPose() != StandPose.IDLE && entity.getStandPose() != StandPose.SUMMON) {
            matrixStack.translate(0, -entity.getEyeHeight(), -0.25D);
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            scale(entity, matrixStack, partialTick);
            doRenderFirstPersonArm(model, handSide, matrixStack, vertexBuilder, packedLight, entity, partialTick);
        }
        else {
            float f = handSide == HandSide.RIGHT ? 1.0F : -1.0F;
            matrixStack.translate((double)(f * 0.85F), -0.9D, -1.2D);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
            matrixStack.translate((double)(f * -1.0F), 3.6D, 3.5D);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
            matrixStack.translate((double)(f * 5.6F), 0.0D, 0.0D);
            model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            doRenderFirstPersonArm(model, handSide, matrixStack, vertexBuilder, packedLight, entity, partialTick);
        }
        matrixStack.popPose();
    }

    protected void doRenderFirstPersonArm(M model, HandSide handSide, 
            MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight, T entity, float partialTick) {
        ModelRenderer armModelRenderer = model.armModel(handSide);
        armModelRenderer.render(matrixStack, vertexBuilder, packedLight, 
                LivingRenderer.getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick)), 1.0F, 1.0F, 1.0F, entity.getAlpha(partialTick));
    }
}
