package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.animnew.INamedModelParts;
import com.github.standobyte.jojo.client.render.entity.model.animnew.ModelPartDefaultState;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.IStandAnimator;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.LegacyStandAnimator;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandPoseData;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry.StandModelRegistryObj;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.IActionAnimation;
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

import net.minecraft.client.Minecraft;
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
    StandModelRegistryObj registryObj;
    
    protected Map<String, ModelPartDefaultState> namedModelParts = new HashMap<>();
    protected Supplier<IStandAnimator> getDefaultGeckoAnimator;
    private IStandAnimator legacyStandAnimHandler;
    public boolean isLayerModel = false;
    
    protected VisibilityMode visibilityMode = VisibilityMode.ALL;
    protected float yRotDeg;
    protected float xRotDeg;
    protected float yRotRad;
    protected float xRotRad;
    protected float ticks;
    public StandPose standPose;

    private boolean initialized = false;
    
    @Deprecated public float idleLoopTickStamp = 0;
    @Deprecated private ModelPose<T> poseReset;
    @Deprecated protected IModelPose<T> idlePose;
    @Deprecated protected IModelPose<T> idleLoop;
    @Deprecated private List<IModelPose<T>> summonPoses;
    @Deprecated protected final Map<StandPose, IActionAnimation<T>> actionAnim = new HashMap<>();
    
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
    
    public final StandModelRegistryObj getRegistryObj() {
        return registryObj;
    }

    public void afterInit() {
        if (!initialized) { 
            initOpposites();
            initPoses();
            initActionPoses();
            legacyStandAnimHandler = new LegacyStandAnimator<>(this, poseReset, idlePose, idleLoop, summonPoses, actionAnim);
            if (registryObj != null && getDefaultGeckoAnimator == null) {
                getDefaultGeckoAnimator = registryObj::getDefaultGeckoAnims;
            }
            initialized = true;
        }
    }
    
    protected void clearAllCubes() {
        headParts().forEach(this::clearAllCubes);
        bodyParts().forEach(this::clearAllCubes);
    }
    
    protected void clearAllCubes(ModelRenderer modelPart) {
        modelPart.cubes.clear();
        modelPart.children.forEach(this::clearAllCubes);
    }
    
    public void setAnimatorSupplier(Supplier<IStandAnimator> supplier) {
        this.getDefaultGeckoAnimator = supplier;
    }
    
    public IStandAnimator getAnimator() {
        if (getDefaultGeckoAnimator != null) {
            IStandAnimator anims = getDefaultGeckoAnimator.get();
            if (anims != null) {
                return anims;
            }
        }
        return legacyStandAnimHandler;
    }
    
    public Supplier<IStandAnimator> getGeckoAnimator() {
        return getDefaultGeckoAnimator;
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
    public void setupAnim(@Nonnull T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        float partialTick = ticks - entity.tickCount;
        StandPoseData poseData = entity.getCurPose(partialTick);
        poseStand(entity, poseData, ticks, yRotationOffset, xRotation);
    }
    
    public void poseStand(@Nullable T entity, StandPoseData pose, float ticks, float yRotOffsetDeg, float xRotDeg) {
        this.yRotDeg = yRotOffsetDeg;
        this.xRotDeg = xRotDeg;
        this.yRotRad = yRotOffsetDeg * MathUtil.DEG_TO_RAD;
        this.xRotRad = xRotDeg * MathUtil.DEG_TO_RAD;
        this.standPose = pose.standPose;
        
        IStandAnimator standAnimator = getAnimator();
        if (standAnimator != null && standAnimator.poseStand(entity, this, pose, ticks, yRotOffsetDeg, xRotDeg)) {}
        else if (standAnimator != legacyStandAnimHandler) {
            legacyStandAnimHandler.poseStand(entity, this, pose, ticks, yRotOffsetDeg, xRotDeg);
        }
        
        this.ticks = ticks;
    }
    
    @Override
    public ModelRenderer getModelPart(String name) {
        ModelPartDefaultState modelPart = namedModelParts.get(name);
        return modelPart != null ? modelPart.modelPart : null;
    }
    
    public void resetPose(@Nullable T entity) {
        namedModelParts.values().forEach(ModelPartDefaultState::reset);
    }


    @Deprecated
    public IActionAnimation<T> dammit(@Nullable T entity, StandPose poseType) {
        return getActionAnim(entity, poseType);
    }
    
    @Deprecated
    protected IActionAnimation<T> getActionAnim(@Nullable T entity, StandPose poseType) {
        return actionAnim.get(poseType);
    }

    @Deprecated
    protected final ModelAnim<T> HEAD_ROTATION = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
        headParts().forEach(part -> {
            part.yRot = MathUtil.rotLerpRad(rotationAmount, part.yRot, yRotOffsetRad);
            part.xRot = MathUtil.rotLerpRad(rotationAmount, part.xRot, xRotRad);
            part.zRot = 0;
        });
    };

    @Deprecated
    protected void poseSummon(T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {}

    public void poseIdleLoop(T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {
        poseStand(entity, StandPoseData.start().standPose(StandPose.IDLE).end(), ticks, yRotOffsetRad, xRotRad);
    }

    @Deprecated
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

    @Deprecated
    protected void initActionPoses() {}

    @Deprecated
    protected abstract ModelPose<T> initPoseReset();

    @Deprecated
    protected IModelPose<T> initBaseIdlePose() {
        return initIdlePose().setAdditionalAnim(HEAD_ROTATION);
    }

    @Deprecated
    protected ModelPose<T> initIdlePose() {
        return initPoseReset();
    }

    @Deprecated
    protected IModelPose<T> initIdlePose2Loop() {
        return initIdlePose();
    }

    @Deprecated
    protected List<IModelPose<T>> initSummonPoses() {
        return Arrays.stream(initSummonPoseRotations())
                .map(rotationAngles -> new ModelPose<T>(rotationAngles))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Deprecated
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[0][0];
    }
    
    
    public void setStandPose(StandPose pose, @Nullable StandEntity entity) {
        if (entity != null) {
            entity.setStandPose(pose);
        }
        this.standPose = pose;
    }
    
    @Deprecated
    public void setCurrentModelAnim(IActionAnimation<T> anim) {}
    
    @Deprecated
    public void onPose(@Nullable T entity, float ticks) {
        idleLoopTickStamp = ticks;
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
    public ModelRenderer getArmNoXRot(HandSide side) {
        return getArm(side);
    }

    @Override
    public abstract Iterable<ModelRenderer> headParts();
    
    @Override
    public abstract Iterable<ModelRenderer> bodyParts();
    
    
    
    protected final void setSecondXRot(ModelRenderer modelPart, float xRot) {
        secondXRotMap.computeIfAbsent(modelPart, part -> new MutableFloat()).setValue(xRot);
    }
    
    protected final void addSecondXRot(ModelRenderer modelPart, float xRot) {
        secondXRotMap.computeIfAbsent(modelPart, part -> new MutableFloat()).add(xRot);
    }
    
    public void resetXRotation() {
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
        entity.getBarrageSwings().updateSwings(Minecraft.getInstance());
        getAnimator().addBarrageSwings(entity, this, ticks);
    }
    
    public void render(T entity, MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        getAnimator().renderBarrageSwings(entity, this, yRotDeg, xRotDeg, 
                matrixStack, buffer, 
                packedLight, packedOverlay, red, green, blue, alpha);
    }
    
    protected void initOpposites() {}
    
    @Override
    public ModelRenderer putNamedModelPart(String name, ModelRenderer modelPart) {
        ModelPartDefaultState modelPartState = ModelPartDefaultState.fromModelPart(modelPart);
        if (modelPartState != null) {
            namedModelParts.put(name, modelPartState);
        }
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
