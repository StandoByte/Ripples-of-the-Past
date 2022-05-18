package com.github.standobyte.jojo.client.model.entity.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.action.actions.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;
import com.github.standobyte.jojo.client.model.pose.StandActionAnimation;
import com.github.standobyte.jojo.client.renderer.entity.stand.AdditionalArmSwing;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.util.utils.MathUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public abstract class StandEntityModel<T extends StandEntity> extends AgeableModel<T> {
    protected VisibilityMode visibilityMode = VisibilityMode.ALL;
    protected float yRotation;
    protected float xRotation;
    protected float ticks;

    protected StandPose poseType = StandPose.SUMMON;
    public float idleLoopTickStamp = 0;
    private ModelPose<T> poseReset;
    protected IModelPose<T> idlePose;
    protected IModelPose<T> idleLoop;
    private List<IModelPose<T>> summonPoses;
    protected final Map<StandPose, StandActionAnimation<T>> actionAnim = new HashMap<>();

    protected StandEntityModel(boolean scaleHead, float yHeadOffset, float zHeadOffset) {
        this(scaleHead, yHeadOffset, zHeadOffset, 2.0F, 2.0F, 24.0F);
    }

    protected StandEntityModel(boolean scaleHead, float yHeadOffset, float zHeadOffset, 
            float babyHeadScale, float babyBodyScale, float bodyYOffset) {
        this(RenderType::entityTranslucent, scaleHead, yHeadOffset, zHeadOffset, babyHeadScale, babyBodyScale, bodyYOffset);
    }

    protected StandEntityModel(Function<ResourceLocation, RenderType> renderType, boolean scaleHead, float yHeadOffset, float zHeadOffset, 
            float babyHeadScale, float babyBodyScale, float bodyYOffset) {
        super(renderType, scaleHead, yHeadOffset, zHeadOffset, babyHeadScale, babyBodyScale, bodyYOffset);
    }

    public void afterInit() {
        initOpposites();
        initPoses();
        initActionPoses();
    }

    protected final void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    public void setVisibilityMode(VisibilityMode mode) {
        this.visibilityMode = mode;
        updatePartsVisibility(mode);
    }

    protected abstract void updatePartsVisibility(VisibilityMode mode);

    @Override
    public void prepareMobModel(T entity, float walkAnimPos, float walkAnimSpeed, float partialTick) {
        StandPose currentPose = entity.getStandPose();
//        if (currentPose != poseType) {
//            resetPose(entity);
//        }
        poseType = currentPose;
    }

    public StandPose getPose() {
        return poseType;
    }

    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        HandSide swingingHand = entity.getSwingingHand();
        headParts().forEach(part -> {
            setRotationAngle(part, 0, 0, 0);
        });

//        initPoses();
//        initActionPoses();

        if (poseType == StandPose.SUMMON && (ticks > SUMMON_ANIMATION_LENGTH || entity.isArmsOnlyMode())) {
            entity.setStandPose(StandPose.IDLE);
            poseType = StandPose.IDLE;
        }

        if (attackTime > 0.0F) {
            idlePose.poseModel(1.0F, entity, ticks, yRotationOffset, xRotation, swingingHand);
            swingArmBarrage(entity, this.attackTime, yRotationOffset, xRotation, ticks, 
                    swingingHand, 0);
        }
        else {
            poseStand(entity, ticks, yRotationOffset, xRotation, 
                    poseType, entity.getCurrentTaskPhase(), 
                    entity.getCurrentTaskCompletion(MathHelper.frac(ticks)), swingingHand);
        }
        this.yRotation = yRotationOffset;
        this.xRotation = xRotation;
        this.ticks = ticks;
        if (!Minecraft.getInstance().isPaused())
        entity.clUpdateSwings(Minecraft.getInstance().getDeltaFrameTime());
    }

    protected void poseStand(T entity, float ticks, float yRotationOffset, float xRotation, 
            StandPose standPose, Optional<Phase> actionPhase, float actionCompletion, HandSide swingingHand) {
        if (actionAnim.containsKey(standPose)) {
            idlePose.poseModel(1.0F, entity, ticks, yRotationOffset, xRotation, swingingHand);
            onPose(entity, ticks);
            
            StandActionAnimation<T> anim = getActionAnim(entity, standPose);
            if (anim != null) {
                anim.animate(actionPhase.get(), actionCompletion, 
                        entity, ticks, yRotationOffset, xRotation, swingingHand);
            }
        }
        else if (standPose == StandPose.SUMMON && summonPoses.size() > 0) {
            poseSummon(entity, ticks, yRotationOffset, xRotation, swingingHand);
        }
        else {
            poseIdleLoop(entity, ticks, yRotationOffset, xRotation, swingingHand);
        }
    }

    protected StandActionAnimation<T> getActionAnim(T entity, StandPose poseType) {
        return actionAnim.get(poseType);
    }

    private void onPose(T entity, float ticks) {
        entity.setYBodyRot(entity.yRot);
        idleLoopTickStamp = ticks;
    }

    protected final ModelAnim<T> HEAD_ROTATION = (rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
        headParts().forEach(part -> {
            part.yRot = MathUtil.rotLerpRad(rotationAmount, part.yRot, yRotationOffset * MathUtil.DEG_TO_RAD);
            part.xRot = MathUtil.rotLerpRad(rotationAmount, part.xRot, xRotation * MathUtil.DEG_TO_RAD);
            part.zRot = 0;
        });
    };
    
    protected void poseSummon(T entity, float ticks, float yRotationOffset, float xRotation, HandSide swingingHand) {
        resetPose(entity);
        onPose(entity, ticks);
        
        summonPoses.get(entity.getSummonPoseRandomByte() % summonPoses.size())
        .poseModel(1.0F, entity, ticks, yRotationOffset, xRotation, swingingHand);

        idlePose.poseModel(summonPoseRotation(ticks), entity, ticks, yRotationOffset, xRotation, swingingHand);
    }
    
    private static float summonPoseRotation(float ticks) {
        return MathHelper.clamp(
                (ticks - SUMMON_ANIMATION_LENGTH) / (SUMMON_ANIMATION_LENGTH * (1 - SUMMON_ANIMATION_POSE_REVERSE_POINT)) + 1, 
                0F, 1F);
    }
    
    protected void poseIdleLoop(T entity, float ticks, float yRotationOffset, float xRotation, HandSide swingingHand) {
        idleLoop.poseModel(ticks - idleLoopTickStamp, entity, ticks, yRotationOffset, xRotation, swingingHand);
    }
    
    protected void initPoses() {
        if (poseReset == null)
            poseReset = initPoseReset();

        if (idlePose == null)
            idlePose = initBaseIdlePose();
        if (idleLoop == null)
            idleLoop = new ModelPoseTransition<T>(idlePose, initIdlePose2Loop()).setEasing(ticks -> MathHelper.sin(ticks / 20));

        if (summonPoses == null)
            summonPoses = initSummonPoses();
    }

    protected void initActionPoses() {}



    protected abstract ModelPose<T> initPoseReset();

    protected IModelPose<T> initBaseIdlePose() {
        return initIdlePose().setAdditionalAnim(HEAD_ROTATION);
    }

    protected ModelPose<T> initIdlePose() {
        return initPoseReset();
    }

    protected IModelPose<T> initIdlePose2Loop() {
        return initIdlePose();
    }

    private static final float SUMMON_ANIMATION_LENGTH = 20.0F;
    private static final float SUMMON_ANIMATION_POSE_REVERSE_POINT = 0.75F;
    protected List<IModelPose<T>> initSummonPoses() {
        return Arrays.stream(initSummonPoseRotations())
                .map(rotationAngles -> new ModelPose<T>(rotationAngles))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[0][0];
    }

    protected abstract void swingArmBarrage(T entity, float swingAmount, float yRotation, float xRotation, float ticks, HandSide swingingHand, float recovery);

    public void renderFirstPersonArms(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}

    public void renderArmSwingHand(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}

    public abstract ModelRenderer armModel(HandSide side);

    // FIXME (!!!!) rapier textures are seemingly bugged when armor is on
    public void renderArmSwings(T entity, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        List<AdditionalArmSwing> swings = entity.getSwingsWithOffsets();
        if (!swings.isEmpty()) {
            resetPose(entity);
            for (AdditionalArmSwing swing : swings) {
                matrixStack.pushPose();
                setVisibilityMode(swing.getSide() == HandSide.LEFT ? VisibilityMode.LEFT_ARM_ONLY : VisibilityMode.RIGHT_ARM_ONLY);
                Vector3d offset = new Vector3d(swing.offset.x, -swing.offset.y, swing.offset.z).xRot(xRotation * MathUtil.DEG_TO_RAD);
                matrixStack.translate(offset.x, offset.y, -offset.z);
                attackTime = swing.getAnim() / AdditionalArmSwing.MAX_ANIM_DURATION;
                HandSide swingingHand = swing.getSide();
                swingArmBarrage(entity, attackTime, yRotation, xRotation, ticks, swingingHand, 0F);
                renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha * 0.5F);
                matrixStack.popPose();
            }
        }
    }

    private void resetPose(T entity) {
        poseReset.poseModel(1, entity, 0, 0, 0, entity.getSwingingHand());
    }
    
    protected void initOpposites() {}
    
    protected final BiMap<ModelRenderer, ModelRenderer> oppositeHandside = HashBiMap.create();
    public final ModelRenderer getOppositeHandside(ModelRenderer modelRenderer) {
        return oppositeHandside.computeIfAbsent(modelRenderer, k -> oppositeHandside.inverse().getOrDefault(modelRenderer, modelRenderer));
    }

    public enum VisibilityMode {
        ALL,
        ARMS_ONLY,
        LEFT_ARM_ONLY,
        RIGHT_ARM_ONLY
    }
}
