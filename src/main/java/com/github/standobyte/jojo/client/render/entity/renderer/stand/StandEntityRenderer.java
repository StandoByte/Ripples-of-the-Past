package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import java.util.Optional;

import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel.VisibilityMode;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.StandGlowLayer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.StandModelLayerRenderer;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

public class StandEntityRenderer<T extends StandEntity, M extends StandEntityModel<T>> extends LivingRenderer<T, M> {
    private final ResourceLocation texture;

    public StandEntityRenderer(EntityRendererManager rendererManager, M entityModel, 
            ResourceLocation texture, float shadowRadius) {
        super(rendererManager, entityModel, shadowRadius);
        this.texture = texture;
        entityModel.afterInit();
        addLayer(new HeldItemLayer<>(this));
        addLayer(new StandGlowLayer<>(this, texture));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return getTextureLocation(entity.getStandSkin());
    }
    
    @Deprecated
    @Override
    public M getModel() {
        return CustomResources.getStandModelOverrides().overrideModel(model);
    }
    
    public M getModel(T entity) {
        return getModel(entity.getStandSkin());
    }
    
    public M getModel(Optional<ResourceLocation> standSkin) {
        M model = getModel();
        M skinModel = StandSkinsManager.getInstance().getStandSkin(standSkin).map(
                skin -> (M) skin.standModels.getOrDefault(model.getModelId(), model)).orElse(model);
        return skinModel;
    }
    
    public ResourceLocation getTextureLocation(Optional<ResourceLocation> standSkin) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(standSkin), texture);
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

    protected RenderType getRenderType(T entity, ResourceLocation modelTexture) {
        return renderType(entity, this.model, modelTexture, 
                isBodyVisible(entity), visibleForSpectator(entity), Minecraft.getInstance().shouldEntityAppearGlowing(entity));
    }

    public RenderType getRenderType(T entity, M model, ResourceLocation modelTexture) {
        return renderType(entity, model, modelTexture, 
                isBodyVisible(entity), visibleForSpectator(entity), Minecraft.getInstance().shouldEntityAppearGlowing(entity));
    }

    protected RenderType renderType(T entity, M model, ResourceLocation modelTexture, 
            boolean isVisible, boolean isVisibleForSpectator, boolean isGlowing) {
        if (isVisible || isVisibleForSpectator) {
            return model.renderType(modelTexture);
        }
        return isGlowing ? RenderType.outline(modelTexture) : null;
    }

    @Deprecated
    @Override
    public RenderType getRenderType(T entity, boolean isVisible, boolean isVisibleForSpectator, boolean isGlowing) {
        return renderType(entity, this.model, getTextureLocation(entity), isVisible, isVisibleForSpectator, isGlowing);
    }
    
    protected float calcAlpha(T entity, float partialTick) {
        if (visibleForSpectator(entity)) {
            return 0.15F;
        }
        return entity.getAlpha(partialTick) * viewObstructionPrevention.alphaFactor;
    }
    
    protected ViewObstructionPrevention obstructsView(T entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson()) {
            Entity user = entity.getUser();
            if (mc.player != null && mc.player.is(user)) {
                if (ClientEventHandler.getInstance().isZooming) {
                    return ViewObstructionPrevention.ARMS_ONLY;
                }
                if (entity.isFollowingUser() && !entity.isArmsOnlyMode()) {
                    Vector3d diffVec = entity.getPosition(partialTick).subtract(user.getPosition(partialTick));
                    Vector3d lookVec = Vector3d.directionFromRotation(0, user.getViewYRot(partialTick));
                    
                    diffVec = new Vector3d(diffVec.x, 0, diffVec.z);
                    lookVec = new Vector3d(lookVec.x, 0, lookVec.z);
                    double distanceSqr = diffVec.lengthSqr();
                    if (distanceSqr < 0.25 || distanceSqr < 1 && lookVec.dot(diffVec) > distanceSqr / 2) {
                        return ViewObstructionPrevention.ARMS_ONLY;
                    }
                }
            }
        }
        return ViewObstructionPrevention.NONE;
    }
    
    protected enum ViewObstructionPrevention {
        NONE(false, 1),
        ARMS_ONLY(true, 1),
        TRANSLUCENCY(false, 0.25F);
        
        private final boolean armsOnly;
        private final float alphaFactor;
        
        private ViewObstructionPrevention(boolean armsOnly, float alphaFactor) {
            this.armsOnly = armsOnly;
            this.alphaFactor = alphaFactor;
        }
    }

    private static final float PLAYER_RENDER_SCALE = 0.9375F;
    @Override
    protected void scale(T entity, MatrixStack matrixStack, float partialTick) {
        matrixStack.scale(
                PLAYER_RENDER_SCALE * entity.getType().getDimensions().width / 0.6F, 
                PLAYER_RENDER_SCALE * entity.getType().getDimensions().height / 1.8F, 
                PLAYER_RENDER_SCALE * entity.getType().getDimensions().width / 0.6F);
    }

    private static final float OVERLAY_TICKS = 10.0F;
    @Override
    protected float getWhiteOverlayProgress(StandEntity entity, float partialTick) {
        return entity.isArmsOnlyMode() || entity.overlayTickCount > OVERLAY_TICKS ? 0 : 
            (OVERLAY_TICKS - MathHelper.clamp(entity.overlayTickCount + partialTick, 0.0F, OVERLAY_TICKS)) / OVERLAY_TICKS;
    }

    private float alpha;
    private ViewObstructionPrevention viewObstructionPrevention;
    @Override
    public void render(T entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<T, M>(entity, this, partialTick, matrixStack, buffer, packedLight))) return;
        M model = getModel(entity);
        matrixStack.pushPose();
        model.attackTime = this.getAttackAnim(entity, partialTick);
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        model.riding = shouldSit;
        model.young = entity.isBaby();
        viewObstructionPrevention = obstructsView(entity, partialTick);
        if (viewObstructionPrevention != ViewObstructionPrevention.NONE) {
            entity.setNoFireAnimFrame();
        }
        model.setVisibility(entity, visibilityMode(entity), viewObstructionPrevention.armsOnly);
        float yBodyRotation = MathHelper.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yHeadRotation = MathHelper.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float yRotationOffset = yHeadRotation - yBodyRotation;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity vehicle = (LivingEntity)entity.getVehicle();
            yBodyRotation = MathHelper.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            float f3 = MathHelper.clamp(MathHelper.wrapDegrees(yRotationOffset - yBodyRotation), -85F, 85F);
            yBodyRotation = yRotationOffset - f3;
            if (Math.abs(f3) > 50F) {
                yBodyRotation += f3 * 0.2F;
            }
            yRotationOffset = yRotationOffset - yBodyRotation;
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
        
        idlePoseSwaying(entity, ticks, matrixStack);
        
        model.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
        model.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        entity.getBarrageSwingsHolder().updateSwings(Minecraft.getInstance());
        model.addBarrageSwings(entity);
        RenderType renderType = getRenderType(entity, getTextureLocation(entity));
        if (renderType != null) {
            IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);
            int packedOverlay = getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick));
            alpha = calcAlpha(entity, partialTick);
            model.render(entity, matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
        }
        
        if (!entity.isSpectator()) {
            for (LayerRenderer<T, M> layerRenderer : this.layers) {
                layerRenderer.render(matrixStack, buffer, packedLight, entity, 
                        walkAnimPos, walkAnimSpeed, partialTick, ticks, yRotationOffset, xRotation);
            }
        }

        matrixStack.popPose();

        RenderNameplateEvent renderNameplateEvent = new RenderNameplateEvent(entity, entity.getDisplayName(), this, matrixStack, buffer, packedLight, partialTick);
        MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
        if (renderNameplateEvent.getResult() != Event.Result.DENY && (renderNameplateEvent.getResult() == Event.Result.ALLOW || this.shouldShowName(entity))) {
            this.renderNameTag(entity, renderNameplateEvent.getContent(), matrixStack, buffer, packedLight);
        }

        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<T, M>(entity, this, partialTick, matrixStack, buffer, packedLight));
    }

    public void renderLayer(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight,
            T entity, float walkAnimSpeed, float walkAnimPos, float partialTick,
            float ticks, float yRotationOffset, float xRotation, M model) {
        getModel(entity).copyPropertiesTo(model);
        model.setVisibility(entity, visibilityMode(entity), viewObstructionPrevention.armsOnly);
        // TODO get rid of these two method calls?
        model.prepareMobModel(entity, walkAnimSpeed, walkAnimPos, partialTick);
        model.setupAnim(entity, walkAnimSpeed, walkAnimPos, ticks, yRotationOffset, xRotation);
        
        int packedOverlay = getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick));
        model.render(entity, matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
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
    
    protected void idlePoseSwaying(T entity, float ticks, MatrixStack matrixStack) {
        M model = getModel(entity);
        if (!entity.isVisibleForAll() && entity.getStandPose() == StandPose.IDLE && 
                model.attackTime == 0 && entity.isFollowingUser()) {
            LivingEntity user = entity.getUser();
            if (!(user != null && user.isShiftKeyDown())) {
                doIdlePoseSwaying(ticks, matrixStack);
            }
        }
    }
    
    protected void doIdlePoseSwaying(float ticks, MatrixStack matrixStack) {
        float idleY = MathHelper.sin((ticks - getModel().idleLoopTickStamp) * 0.04F) * 0.04F;
        matrixStack.translate(0.0D, idleY, 0.0D);
    }
    
    
    public void renderFirstPersonArms(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, float partialTick) {
        if (entity.getStandPose().armsObstructView) return;
        
        getModel(entity).setVisibility(entity, VisibilityMode.ARMS_ONLY, false);
        renderFirstPersonArm(HandSide.LEFT, matrixStack, buffer, packedLight, entity, partialTick);
        renderFirstPersonArm(HandSide.RIGHT, matrixStack, buffer, packedLight, entity, partialTick);
    }

    protected void renderFirstPersonArm(HandSide handSide, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, float partialTick) {
        RenderType renderType = getRenderType(entity, getTextureLocation(entity));
        if (renderType != null) {
            renderSeparateLayerArm(getModel(entity), handSide, matrixStack, buffer.getBuffer(renderType), packedLight, entity, partialTick);
            for (LayerRenderer<T, M> layer : layers) {
                if (layer instanceof StandModelLayerRenderer) {
                    StandModelLayerRenderer<T, M> standLayer = (StandModelLayerRenderer<T, M>) layer;
                    if (standLayer.shouldRender(entity, entity.getStandSkin())) {
                        RenderType layerRenderType = standLayer.getRenderType(entity);
                        if (layerRenderType != null) {
                            renderSeparateLayerArm(standLayer.getLayerModel(entity), handSide, matrixStack, 
                                    buffer.getBuffer(layerRenderType), standLayer.getPackedLight(packedLight), entity, partialTick);
                        }
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
        if (entity.getType() != ModStands.GOLD_EXPERIENCE.getEntityType()) // FIXME !!!!!!!! fix stand first person view
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
        ModelRenderer armModelRenderer = model.getArm(handSide);
        armModelRenderer.render(matrixStack, vertexBuilder, packedLight, 
                LivingRenderer.getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick)), 1.0F, 1.0F, 1.0F, entity.getAlpha(partialTick));
    }
    
    
    public void renderIdleWithSkin(MatrixStack matrixStack, StandSkin standSkin, IRenderTypeBuffer buffer, float ticks) {
        matrixStack.pushPose();
        Optional<ResourceLocation> nonDefaultSkin = standSkin.getNonDefaultLocation();
        M model = getModel(nonDefaultSkin);
        model.attackTime = 0;
        model.riding = false;
        model.young = false;
        viewObstructionPrevention = ViewObstructionPrevention.NONE;
        model.updatePartsVisibility(VisibilityMode.ALL);
        
        float yRotationOffset = 0;
        float xRotation = 0;
        float partialTick = MathHelper.frac(ticks);
        
        doIdlePoseSwaying(ticks, matrixStack);
        model.prepareMobModel(null, 0, 0, partialTick);
        model.poseIdleLoop(null, ticks, yRotationOffset, xRotation, HandSide.RIGHT);
        
        ResourceLocation texture = getTextureLocation(nonDefaultSkin);
        RenderType renderType = model.renderType(texture);
        int packedLight = ClientUtil.MAX_MODEL_LIGHT;
        if (renderType != null) {
            IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);
            int packedOverlay = OverlayTexture.NO_OVERLAY;
            model.renderToBuffer(matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            
            for (LayerRenderer<T, M> layerRenderer : this.layers) {
                if (layerRenderer instanceof StandModelLayerRenderer) {
                    StandModelLayerRenderer<T, M> standLayer = (StandModelLayerRenderer<T, M>) layerRenderer;
                    if (standLayer.shouldRender(null, nonDefaultSkin)) {
                        M layerModel = standLayer.getLayerModel(nonDefaultSkin);
                        RenderType layerRenderType = layerModel.renderType(standLayer.getLayerTexture(nonDefaultSkin));
                        IVertexBuilder layerVertexBuilder = buffer.getBuffer(layerRenderType);
                        layerModel.attackTime = 0;
                        layerModel.riding = false;
                        layerModel.young = false;
                        layerModel.updatePartsVisibility(VisibilityMode.ALL);
                        layerModel.prepareMobModel(null, 0, 0, partialTick);
                        layerModel.poseIdleLoop(null, ticks, yRotationOffset, xRotation, HandSide.RIGHT);
                        layerModel.renderToBuffer(matrixStack, layerVertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
                    }
                }
            }
        }

        matrixStack.popPose();
    }
}
