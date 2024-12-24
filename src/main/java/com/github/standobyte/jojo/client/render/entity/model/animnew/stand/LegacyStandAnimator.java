package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.anim.IActionAnimation;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.IBarrageAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class LegacyStandAnimator<T extends StandEntity> implements IStandAnimator {
    private StandEntityModel<T> model;
    private ModelPose<T> poseReset;
    protected IModelPose<T> idlePose;
    protected IModelPose<T> idleLoop;
    private List<IModelPose<T>> summonPoses;
    protected Map<StandPose, IActionAnimation<T>> actionAnim = new HashMap<>();
    @Nullable private IActionAnimation<T> currentActionAnim = null;
    
    public LegacyStandAnimator(
            StandEntityModel<T> model,
            ModelPose<T> poseReset,
            IModelPose<T> idlePose,
            IModelPose<T> idleLoop,
            List<IModelPose<T>> summonPoses,
            Map<StandPose, IActionAnimation<T>> actionAnim) {
        this.model = model;
        this.poseReset = poseReset;
        this.idlePose = idlePose;
        this.idleLoop = idleLoop;
        this.summonPoses = summonPoses;
        this.actionAnim = actionAnim;
    }

    @Override
    public <A extends StandEntity> boolean poseStand(@Nullable A standEntity, StandEntityModel<A> standEntityModel, StandPoseData poseData, 
            float ticks, float yRotOffsetDeg, float xRotDeg) {
        float yRotOffsetRad = yRotOffsetDeg * MathUtil.DEG_TO_RAD;
        float xRotRad = xRotDeg * MathUtil.DEG_TO_RAD;
        T entity = (T) standEntity;
        StandEntityModel<T> model = (StandEntityModel<T>) standEntityModel;
        currentActionAnim = null;
        
        model.resetXRotation();
        model.headParts().forEach(part -> ClientUtil.setRotationAngle(part, 0, 0, 0));
        model.bodyParts().forEach(part -> ClientUtil.setRotationAngle(part, 0, 0, 0));
        
        StandPose standPose = poseData.standPose;
        if (standPose == StandPose.SUMMON && ticks > SUMMON_ANIMATION_LENGTH) {
            standPose = StandPose.IDLE;
            model.setStandPose(standPose, entity);
        }
        
        HandSide swingingHand = entity != null ? entity.getPunchingHand() : HandSide.RIGHT;
        if (actionAnim.containsKey(standPose)) {
            idlePose.poseModel(1.0F, entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
            model.onPose(entity, ticks);
            
            IActionAnimation<T> anim = model.dammit(entity, standPose);
            model.setCurrentModelAnim(anim);
            this.currentActionAnim = anim;
            if (anim != null) {
                anim.animate(poseData.actionPhase.orElse(null), poseData.phaseCompletion, 
                        entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
            }
        }
        else if (standPose == StandPose.SUMMON && summonPoses.size() > 0) {
            poseSummon(entity, model, ticks, yRotOffsetRad, xRotRad, swingingHand);
        }
        else {
            poseIdleLoop(entity, model, ticks, yRotOffsetRad, xRotRad, swingingHand);
        }
        
        model.applyXRotation();
        return true;
    }
    
    private static final float SUMMON_ANIMATION_LENGTH = 20.0F;
    private static final float SUMMON_ANIMATION_POSE_REVERSE_POINT = 0.75F;
    
    private static float summonPoseRotation(float ticks) {
        return MathHelper.clamp(
                (ticks - SUMMON_ANIMATION_LENGTH) / (SUMMON_ANIMATION_LENGTH * (1 - SUMMON_ANIMATION_POSE_REVERSE_POINT)) + 1, 
                0F, 1F);
    }

    public void resetPose(T entity) {
        poseReset.poseModel(1, entity, 0, 0, 0, entity.getPunchingHand());
    }
    
    protected void poseSummon(T entity, StandEntityModel<T> model, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {
        resetPose(entity);
        model.onPose(entity, ticks);
        
        summonPoses.get(entity.getSummonPoseRandomByte() % summonPoses.size())
        .poseModel(1.0F, entity, ticks, yRotOffsetRad, xRotRad, swingingHand);

        idlePose.poseModel(summonPoseRotation(ticks), entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
    }
    
    public void poseIdleLoop(T entity, StandEntityModel<T> model, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {
        idleLoop.poseModel(ticks - model.idleLoopTickStamp, entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
    }
    
    
    @Override
    public <A extends StandEntity> void addBarrageSwings(A entity, StandEntityModel<A> model, float ticks) {
        if (entity.getStandPose() == StandPose.BARRAGE && entity.getCurrentTaskPhase().map(phase -> phase == Phase.PERFORM).orElse(false)
                && currentActionAnim instanceof IBarrageAnimation) {
            ((IBarrageAnimation<T, StandEntityModel<T>>) currentActionAnim).addSwings((T) entity, entity.getPunchingHand(), ticks);
        }
    }
    
    @Override
    public <A extends StandEntity> void renderBarrageSwings(A entity, StandEntityModel<A> model, float yRotOffsetDeg, float xRotDeg, 
            MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!model.isLayerModel) {
            BarrageSwingsHolder<T, StandEntityModel<T>> barrageSwings = (BarrageSwingsHolder<T, StandEntityModel<T>>) entity.getBarrageSwingsHolder();
            barrageSwings.renderBarrageSwings((StandEntityModel<T>) model, (T) entity, matrixStack, buffer, 
                    packedLight, packedOverlay, yRotOffsetDeg * MathUtil.DEG_TO_RAD, xRotDeg * MathUtil.DEG_TO_RAD, red, green, blue, alpha);
        }
    }

}
