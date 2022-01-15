package com.github.standobyte.jojo.client.model.entity.stand;

import java.util.List;
import java.util.function.Function;

import com.github.standobyte.jojo.action.actions.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.renderer.entity.stand.AdditionalArmSwing;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.util.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public abstract class StandEntityModel<T extends StandEntity> extends AgeableModel<T> {
    protected VisibilityMode visibilityMode = VisibilityMode.ALL;
    protected StandPose poseType = StandPose.SUMMON;
    private float xRotation;

    protected StandEntityModel(boolean scaleHead, float yHeadOffset, float zHeadOffset) {
        this(scaleHead, yHeadOffset, zHeadOffset, 2.0F, 2.0F, 24.0F);
    }

    protected StandEntityModel(boolean scaleHead, float yHeadOffset, float zHeadOffset, 
            float babyHeadScale, float babyBodyScale, float bodyYOffset) {
        super(texture -> RenderType.entityTranslucent(texture), scaleHead, yHeadOffset, zHeadOffset, babyHeadScale, babyBodyScale, bodyYOffset);
    }

    protected StandEntityModel(Function<ResourceLocation, RenderType> renderType, boolean scaleHead, float yHeadOffset, float zHeadOffset, 
            float babyHeadScale, float babyBodyScale, float bodyYOffset) {
        super(renderType, scaleHead, yHeadOffset, zHeadOffset, babyHeadScale, babyBodyScale, bodyYOffset);
    }

    protected final void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    protected void setSummonPoseRotationAngle(ModelRenderer modelRenderer, float x, float y, float z, float factor, 
            float xIdle, float yIdle, float zIdle) {
        setRotationAngle(modelRenderer, 
                isHead(modelRenderer) 
                ? modelRenderer.xRot + xIdle + (x - xIdle) * factor
                : xIdle + (x - xIdle) * factor, 
                yIdle + (y - yIdle) * factor, 
                zIdle + (z - zIdle) * factor);
    }

    protected void setSummonPoseRotationAngle(ModelRenderer modelRenderer, float x, float y, float z, float factor) {
        setRotationAngle(modelRenderer, 
                isHead(modelRenderer)
                ? modelRenderer.xRot + x * factor 
                : x * factor, 
                y * factor, 
                z * factor);
    }
    
    protected abstract boolean isHead(ModelRenderer modelRenderer);
    
    public void setVisibilityMode(VisibilityMode mode, boolean forearmOnly) {
        this.visibilityMode = mode;
        updatePartsVisibility(mode, forearmOnly);
    }

    protected abstract void updatePartsVisibility(VisibilityMode mode, boolean forearmOnly);

    @Override
    public void prepareMobModel(T entity, float walkAnimPos, float walkAnimSpeed, float partialTick) {
        StandPose currentPose = entity.getStandPose();
        if (currentPose != poseType) {
            resetPose();
        }
        poseType = currentPose;
    }

    public StandPose getPose() {
        return poseType;
    }

    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        headParts().forEach(part -> {
            part.yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
            part.xRot = xRotation * MathUtil.DEG_TO_RAD;
            part.zRot = 0;
        });
        if (this.attackTime > 0.0F) {
            swingArm(entity, xRotation, entity.swingingArm == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite());
        }
        else {
            if (poseType == StandPose.SUMMON) {
                if (ticks > SUMMON_ANIMATION_LENGTH || entity.isArmsOnlyMode()) {
                    entity.setStandPose(StandPose.NONE);
                }
                else {
                    entity.setYBodyRot(entity.yRot);
                    summonAnimation(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
                }
            }
            else if (poseType == StandPose.NONE) {
                resetPose();
            }
            else if (poseType == StandPose.BLOCK) {
                blockingPose(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
            }
            else if (poseType == StandPose.RANGED_ATTACK) {
                rangedAttackPose(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation, entity.getCurrentTaskPhase());
            }
            else if (poseType == StandPose.ABILITY) {
                specialAbilityPose(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation, entity.getCurrentTaskPhase());
            }
            else {
                customPose(poseType, entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation, entity.getCurrentTaskPhase());
            }
        }
        this.xRotation = xRotation;
        /*if (!Minecraft.getInstance().isPaused())*/ entity.clUpdateSwings(Minecraft.getInstance().getDeltaFrameTime());
    }
    
    private static final float SUMMON_ANIMATION_LENGTH = 20.0F;
    private static final float SUMMON_ANIMATION_POSE_REVERSE_POINT = 0.75F;
    protected void summonAnimation(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        summonPose(ticks > SUMMON_ANIMATION_LENGTH * SUMMON_ANIMATION_POSE_REVERSE_POINT ? 
                (SUMMON_ANIMATION_LENGTH - ticks) / (SUMMON_ANIMATION_LENGTH * (1 - SUMMON_ANIMATION_POSE_REVERSE_POINT))
                : 1.0F, entity.getSummonPoseRandomByte() % getSummonPosesCount());
    }

    protected abstract void resetPose();
    protected void summonPose(float animationFactor, int poseVariant) {
        resetPose();
    }
    protected int getSummonPosesCount() {
        return 1;
    }
    protected abstract void blockingPose(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation);
    protected void rangedAttackPose(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
        resetPose();
    }
    protected void specialAbilityPose(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
        resetPose();
    }
    protected void customPose(StandPose pose, T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
        resetPose();
    }
    protected abstract void swingArm(T entity, float xRotation, HandSide swingingHand);

    public void renderFirstPersonArms(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}

    public void renderArmSwingHand(HandSide handSide, MatrixStack matrixStack, 
            IVertexBuilder buffer, int packedLight, T entity, float partialTick, 
            int packedOverlay, float red, float green, float blue, float alpha) {}

    public abstract ModelRenderer armModel(HandSide side);
    
    public void renderArmSwings(T entity, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        List<AdditionalArmSwing> swings = entity.getSwingsWithOffsets();
        if (!swings.isEmpty()) {
            resetPose();
            for (AdditionalArmSwing swing : swings) {
                matrixStack.pushPose();
                setVisibilityMode(swing.getSide() == HandSide.LEFT ? VisibilityMode.LEFT_ARM_ONLY : VisibilityMode.RIGHT_ARM_ONLY, true);
                matrixStack.translate(swing.offset.x, swing.offset.y, swing.offset.z);
                float anim = swing.getAnim();
                HandSide swingingHand;
                if (anim <= 1) {
                    attackTime = anim;
                    swingingHand = swing.getSide();
                }
                else {
                    attackTime = anim - 1;
                    swingingHand = swing.getSide().getOpposite();
                }
                swingArm(entity, xRotation, swingingHand);
                rotateAdditionalArmSwings();
                renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha * 0.5F);
                matrixStack.popPose();
            }
        }
    }
    
    protected abstract void rotateAdditionalArmSwings();

    public enum VisibilityMode {
        ALL,
        ARMS_ONLY,
        LEFT_ARM_ONLY,
        RIGHT_ARM_ONLY
    }
}
