package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.animnew.INamedModelParts;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.IActionAnimation;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.IBarrageAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.TargetHitPart;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public abstract class StandEntityModel<T extends StandEntity> extends AgeableModel<T> implements IHasArm, INamedModelParts {
    protected static final Random RANDOM = new Random();
    ResourceLocation modelId = null;
    protected Map<String, ModelRenderer> namedModelParts = new HashMap<>();
    
    protected VisibilityMode visibilityMode = VisibilityMode.ALL;
    protected float yRotRad;
    protected float xRotRad;
    protected float ticks;
    protected StandPose standPose;

    private boolean initialized = false;
    public float idleLoopTickStamp = 0;
    private ModelPose<T> poseReset;
    protected IModelPose<T> idlePose;
    protected IModelPose<T> idleLoop;
    private List<IModelPose<T>> summonPoses;
    protected final Map<StandPose, IActionAnimation<T>> actionAnim = new HashMap<>();
    @Nullable
    private IActionAnimation<T> currentActionAnim = null;
    
    private Map<ModelRenderer, MutableFloat> secondXRotMap = new HashMap<>();
    
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
    
    public final ResourceLocation getModelId() {
        return modelId;
    }

    public void afterInit() {
        if (!initialized) { 
            initOpposites();
            initPoses();
            initActionPoses();
            initialized = true;
        }
    }

    public static final void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
    
    public void setVisibility(T entity, VisibilityMode mode, boolean obstructsView) {
        setVisibility(entity, mode, obstructsView, false);
    }

    public void setVisibility(T entity, VisibilityMode mode, boolean obstructsView, boolean standFirstPersonRender) {
        if (obstructsView || standFirstPersonRender) {
            if (entity.getStandPose().armsObstructView && !standFirstPersonRender) {
                mode = mode.reduceTo(VisibilityMode.NONE);
            }
            else {
                mode = mode.reduceTo(VisibilityMode.ARMS_ONLY);
            }
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
    
    public void updatePartsVisibility(VisibilityMode mode) {}
    protected abstract void partMissing(StandPart standPart);
    
    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        resetXRotation();
        
        HandSide swingingHand = entity.getPunchingHand();
        headParts().forEach(part -> {
            setRotationAngle(part, 0, 0, 0);
        });
        bodyParts().forEach(part -> {
            setRotationAngle(part, 0, 0, 0);
        });

//        initPoses();
//        initActionPoses();

        StandPose pose = entity.getStandPose();
        if (pose == StandPose.SUMMON && (ticks > SUMMON_ANIMATION_LENGTH || entity.isArmsOnlyMode())) {
            entity.setStandPose(StandPose.IDLE);
            pose = StandPose.IDLE;
        }
        this.standPose = pose;
        
        this.yRotRad = yRotationOffset * MathUtil.DEG_TO_RAD;
        this.xRotRad = xRotation * MathUtil.DEG_TO_RAD;
        poseStand(entity, ticks, yRotRad, xRotRad, 
                pose, entity.getCurrentTaskPhase(), 
                entity.getCurrentTaskPhaseCompletion(ticks - entity.tickCount), swingingHand);
        this.ticks = ticks;
        
        applyXRotation();
    }
    
    protected void poseStand(T entity, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        
//        Animation newIdleAnim = CustomResources.getStandModelAnimations().getAnim(getModelId(), "idle");
//        if (newIdleAnim != null) {
//            StandAnimator.animate(this, newIdleAnim, ticks - idleLoopTickStamp, 1);
//        }
        
        legacyPoseStand(entity, ticks, yRotOffsetRad, xRotRad, standPose, actionPhase, phaseCompletion, swingingHand);
    }
    
    public ModelRenderer getModelPart(String name) {
        return namedModelParts.get(name);
    }
    
    
    
    protected void legacyPoseStand(T entity, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        if (actionAnim.containsKey(standPose)) {
            idlePose.poseModel(1.0F, entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
            onPose(entity, ticks);
            
            currentActionAnim = getActionAnim(entity, standPose);
            if (currentActionAnim != null) {
                currentActionAnim.animate(actionPhase.get(), phaseCompletion, 
                        entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
            }
        }
        else if (standPose == StandPose.SUMMON && summonPoses.size() > 0) {
            poseSummon(entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
        }
        else {
            poseIdleLoop(entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
        }
    }

    protected IActionAnimation<T> getActionAnim(T entity, StandPose poseType) {
        return actionAnim.get(poseType);
    }

    private void onPose(T entity, float ticks) {
        idleLoopTickStamp = ticks;
    }

    protected final ModelAnim<T> HEAD_ROTATION = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
        headParts().forEach(part -> {
            part.yRot = MathUtil.rotLerpRad(rotationAmount, part.yRot, yRotOffsetRad);
            part.xRot = MathUtil.rotLerpRad(rotationAmount, part.xRot, xRotRad);
            part.zRot = 0;
        });
    };
    
    protected void poseSummon(T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {
        resetPose(entity);
        onPose(entity, ticks);
        
        summonPoses.get(entity.getSummonPoseRandomByte() % summonPoses.size())
        .poseModel(1.0F, entity, ticks, yRotOffsetRad, xRotRad, swingingHand);

        idlePose.poseModel(summonPoseRotation(ticks), entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
    }
    
    private static float summonPoseRotation(float ticks) {
        return MathHelper.clamp(
                (ticks - SUMMON_ANIMATION_LENGTH) / (SUMMON_ANIMATION_LENGTH * (1 - SUMMON_ANIMATION_POSE_REVERSE_POINT)) + 1, 
                0F, 1F);
    }
    
    public void poseIdleLoop(T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {
        idleLoop.poseModel(ticks - idleLoopTickStamp, entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
    }
    
    protected void initPoses() {
        if (poseReset == null)
            poseReset = initPoseReset();

        if (idlePose == null)
            idlePose = initBaseIdlePose();
        if (idleLoop == null)
            idleLoop = new ModelPoseTransition<T>(idlePose, initIdlePose2Loop())
                .setEasing(ticks -> (MathHelper.sin((float) Math.PI * (ticks / 40 - 0.5f)) - 1) / 2);

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
    
    @Deprecated
    public void renderFirstPersonArms(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}

    @Deprecated
    public void renderArmSwingHand(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}
    
    public void setupFirstPersonRotations(MatrixStack matrixStack, T entity, float xRot, float yRot, float yBodyRot) {
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180 + yBodyRot));
        matrixStack.translate(0, -entity.getEyeHeight(), 0);
    }

    public abstract ModelRenderer getArm(HandSide side);
    
    
    
    protected final void setSecondXRot(ModelRenderer modelPart, float xRot) {
        secondXRotMap.computeIfAbsent(modelPart, part -> new MutableFloat()).setValue(xRot);
    }
    
    protected final void addSecondXRot(ModelRenderer modelPart, float xRot) {
        secondXRotMap.computeIfAbsent(modelPart, part -> new MutableFloat()).add(xRot);
    }
    
    private void resetXRotation() {
        secondXRotMap.forEach((modelPart, xRotMutable) -> xRotMutable.setValue(0));
    }
    
    public void applyXRotation() {
        secondXRotMap.forEach((modelPart, xRotMutable) -> {
            float xRot = xRotMutable.getValue();
            if (xRot != 0) {
                ClientUtil.rotateAngles(modelPart, xRot);
            }
        });
    }
    
    public void addBarrageSwings(T entity) {
        if (entity.getStandPose() == StandPose.BARRAGE && entity.getCurrentTaskPhase().map(phase -> phase == Phase.PERFORM).orElse(false)
                && currentActionAnim instanceof IBarrageAnimation) {
            ((IBarrageAnimation<T, StandEntityModel<T>>) currentActionAnim).addSwings(entity, entity.getPunchingHand(), ticks);
        }
    }
    
    public void render(T entity, MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//        if (currentActionAnim != null) {
//            currentActionAnim.renderAdditional(entity, matrixStack, buffer, 
//                    packedLight, packedOverlay, red, green, blue, alpha);
//        }
    }
    
    public final void renderBarrageSwings(T entity, MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        renderBarrageSwings(entity, this, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    
    protected <M extends StandEntityModel<T>> void renderBarrageSwings(T entity, M thisModel, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight,
            int packedOverlay, float red, float green, float blue, float alpha) {
        BarrageSwingsHolder<T, M> barrageSwings = (BarrageSwingsHolder<T, M>) entity.getBarrageSwingsHolder();
        barrageSwings.renderBarrageSwings(thisModel, entity, matrixStack, buffer, 
                packedLight, packedOverlay, yRotRad, xRotRad, red, green, blue, alpha);
    }

    public void resetPose(T entity) {
        poseReset.poseModel(1, entity, 0, 0, 0, entity.getPunchingHand());
    }
    
    protected void initOpposites() {}
    
    @Override
    public ModelRenderer putMamedModelPart(String name, ModelRenderer modelPart) {
        namedModelParts.put(name, modelPart);
        return modelPart;
    }
    
    protected final BiMap<ModelRenderer, ModelRenderer> oppositeHandside = HashBiMap.create();
    public final ModelRenderer getOppositeHandside(ModelRenderer modelRenderer) {
        return oppositeHandside.computeIfAbsent(modelRenderer, k -> oppositeHandside.inverse().getOrDefault(modelRenderer, modelRenderer));
    }
    
    
    @Nullable
    public ModelRenderer.ModelBox getRandomCubeAt(TargetHitPart entityPart) {
        return null;
    }
    
    
    public enum VisibilityMode {
        ALL,
        ARMS_ONLY,
        LEFT_ARM_ONLY,
        RIGHT_ARM_ONLY,
        BODY_WITHOUT_ARMS(ARMS_ONLY),
        BODY_WITH_LEFT_ARM(RIGHT_ARM_ONLY),
        BODY_WITH_RIGHT_ARM(LEFT_ARM_ONLY),
        NONE(ALL);
        
        public final VisibilityMode baseMode;
        private VisibilityMode inverse;
        public final boolean isInverted;
        
        private VisibilityMode() {
            this.baseMode = this;
            this.isInverted = false;
        }
        
        private VisibilityMode(VisibilityMode inverting) {
            this.baseMode = inverting;
            this.isInverted = true;
            this.inverse = inverting;
            inverting.inverse = this;
        }
        
        public VisibilityMode invert() {
            return inverse;
        }
        
        public VisibilityMode invert(boolean doInvert) {
            return doInvert ? invert() : this;
        }
        
        public VisibilityMode reduceTo(VisibilityMode targetBaseMode) {
            if (targetBaseMode.isInverted && targetBaseMode != VisibilityMode.NONE) {
                throw new IllegalArgumentException();
            }
            if (targetBaseMode == VisibilityMode.ALL || targetBaseMode == this.baseMode) {
                return this;
            }
            
            switch (this.baseMode) {
            case NONE:
                return this;
            case ALL:
                break;
            case LEFT_ARM_ONLY:
                if (targetBaseMode == RIGHT_ARM_ONLY) {
                    targetBaseMode = VisibilityMode.NONE;
                }
                break;
            case RIGHT_ARM_ONLY:
                if (targetBaseMode == LEFT_ARM_ONLY) {
                    targetBaseMode = VisibilityMode.NONE;
                }
                break;
            default:
                throw new IllegalStateException();
            }
            
            if (this.isInverted) {
                targetBaseMode = targetBaseMode.inverse;
            }
            return targetBaseMode;
        }
    }
}
