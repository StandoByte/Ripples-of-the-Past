package com.github.standobyte.jojo.client.render.entity.pose.anim;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.RigidModelPose;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class PosedActionAnimation<T extends Entity> implements IActionAnimation<T> {
    private final Map<StandEntityAction.Phase, IModelPose<T>> phasePoses;
    
    private PosedActionAnimation(PosedActionAnimation.Builder<T> builder, IModelPose<T> idlePose) {
        this.phasePoses = builder.phasePoses;
        StandEntityAction.Phase[] phases = StandEntityAction.Phase.values();
        int iLastMissing = -1;
        for (int i = phases.length - 1; i >= 0; i--) {
            StandEntityAction.Phase phase = phases[i];
            boolean hasPose = phasePoses.containsKey(phase);
            
            if (iLastMissing == -1 && !hasPose) {
                iLastMissing = i;
            }
            if ((i == 0 || hasPose) && iLastMissing > -1) {
                IModelPose<T> lastPose = hasPose ? 
                        phasePoses.get(phase)
                        : idlePose;
                for (int j = hasPose ? i + 1 : i; j <= iLastMissing; j++) {
                    StandEntityAction.Phase missingPhase = phases[j];
                    IModelPose<T> fillerPose = missingPhase == StandEntityAction.Phase.RECOVERY ? 
                                    new ModelPoseTransition<T>(lastPose, idlePose)
                                    : new RigidModelPose<T>(lastPose);
                    phasePoses.put(missingPhase, fillerPose);
                }
                iLastMissing = -1;
            }
        }
    }
    
    @Override
    public void animate(StandEntityAction.Phase phase, float phaseCompletion, 
            T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide side) {
        phasePoses.get(phase).poseModel(phaseCompletion, entity, ticks, yRotOffsetRad, xRotRad, side);
    }

    
    
    public static class Builder<T extends Entity> {
        private final Map<StandEntityAction.Phase, IModelPose<T>> phasePoses = new EnumMap<>(StandEntityAction.Phase.class);
        
        public Builder<T> addPose(StandEntityAction.Phase phase, IModelPose<T> pose) {
            phasePoses.put(phase, pose);
            return this;
        }
        
        public PosedActionAnimation<T> build(IModelPose<T> idlePose) {
            return new PosedActionAnimation<T>(this, idlePose);
        }
    }
}
