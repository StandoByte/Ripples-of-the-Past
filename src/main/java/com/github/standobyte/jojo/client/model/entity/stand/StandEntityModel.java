package com.github.standobyte.jojo.client.model.entity.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;
import com.github.standobyte.jojo.client.model.pose.anim.IActionAnimation;
import com.github.standobyte.jojo.client.model.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.utils.MathUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public abstract class StandEntityModel<T extends StandEntity> extends AgeableModel<T> implements IHasArm {
    protected VisibilityMode visibilityMode = VisibilityMode.ALL;
    protected float yRotation;
    protected float xRotation;
    protected float ticks;
    public boolean layerRenderer;

    public float idleLoopTickStamp = 0;
    private ModelPose<T> poseReset;
    protected IModelPose<T> idlePose;
    protected IModelPose<T> idleLoop;
    private List<IModelPose<T>> summonPoses;
    protected final Map<StandPose, IActionAnimation<T>> actionAnim = new HashMap<>();
    @Nullable
    private IActionAnimation<T> currentActionAnim = null;

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

    public void setVisibility(T entity, VisibilityMode mode, boolean obstructsView) {
        if (obstructsView) {
            mode = entity.getStandPose().armsObstructView ? VisibilityMode.NONE : VisibilityMode.ARMS_ONLY;
        }
        this.visibilityMode = mode;
        updatePartsVisibility(mode);
        
        IStandPower standPower = entity.getUserPower();
        if (standPower != null) {
            standPower.getStandInstance().ifPresent(standInstance -> {
                for (StandPart part : StandPart.values()) {
                    if (!standInstance.hasPart(part)) {
                        partMissing(part);
                    }
                }
            });
        }
    }

    protected abstract void updatePartsVisibility(VisibilityMode mode);
    protected abstract void partMissing(StandPart standPart);
    
    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        HandSide swingingHand = entity.getPunchingHand();
        headParts().forEach(part -> {
            setRotationAngle(part, 0, 0, 0);
        });

//        initPoses();
//        initActionPoses();

        StandPose pose = entity.getStandPose();
        if (pose == StandPose.SUMMON && (ticks > SUMMON_ANIMATION_LENGTH || entity.isArmsOnlyMode())) {
            entity.setStandPose(StandPose.IDLE);
            pose = StandPose.IDLE;
        }

        poseStand(entity, ticks, yRotationOffset, xRotation, 
                pose, entity.getCurrentTaskPhase(), 
                entity.getCurrentTaskPhaseCompletion(ticks - entity.tickCount), swingingHand);
        this.yRotation = yRotationOffset;
        this.xRotation = xRotation;
        this.ticks = ticks;
        if (!layerRenderer) entity.getBarrageSwingsHolder().updateSwings(Minecraft.getInstance());
    }

    protected void poseStand(T entity, float ticks, float yRotationOffset, float xRotation, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        if (actionAnim.containsKey(standPose)) {
            idlePose.poseModel(1.0F, entity, ticks, yRotationOffset, xRotation, swingingHand);
            onPose(entity, ticks);
            
            currentActionAnim = getActionAnim(entity, standPose);
            if (currentActionAnim != null) {
                currentActionAnim.animate(actionPhase.get(), phaseCompletion, 
                        entity, ticks, yRotationOffset, xRotation, swingingHand, layerRenderer);
            }
        }
        else if (standPose == StandPose.SUMMON && summonPoses.size() > 0) {
            poseSummon(entity, ticks, yRotationOffset, xRotation, swingingHand);
        }
        else {
            poseIdleLoop(entity, ticks, yRotationOffset, xRotation, swingingHand);
        }
    }

    protected IActionAnimation<T> getActionAnim(T entity, StandPose poseType) {
        return actionAnim.get(poseType);
    }

    private void onPose(T entity, float ticks) {
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

    public void renderFirstPersonArms(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}

    public void renderArmSwingHand(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}

    public abstract ModelRenderer getArm(HandSide side);


    
    
    public void render(T entity, MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
//        if (currentActionAnim != null) {
//            currentActionAnim.renderAdditional(entity, matrixStack, buffer, 
//                    packedLight, packedOverlay, red, green, blue, alpha);
//        }
        renderBarrageSwings(entity, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    protected void renderBarrageSwings(T entity, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight,
            int packedOverlay, float red, float green, float blue, float alpha) {
        BarrageSwingsHolder<T> barrageSwings = (BarrageSwingsHolder<T>) entity.getBarrageSwingsHolder();
        barrageSwings.renderBarrageSwings(this, entity, matrixStack, buffer, 
                packedLight, packedOverlay, yRotation, xRotation, red, green, blue, alpha);
    }

    public void resetPose(T entity) {
        poseReset.poseModel(1, entity, 0, 0, 0, entity.getPunchingHand());
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
        RIGHT_ARM_ONLY,
        NONE
    }
}
