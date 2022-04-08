package com.github.standobyte.jojo.client.model.pose;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.action.actions.StandEntityAction;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class StandActionAnimation<T extends Entity> {
    private final Map<StandEntityAction.Phase, IModelPose<T>> phasePoses;
    
    private StandActionAnimation(StandActionAnimation.Builder<T> builder, IModelPose<T> idlePose) {
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
    
    public void animate(StandEntityAction.Phase phase, float actionCompletion, 
            T entity, float ticks, float yRotationOffset, float xRotation, HandSide side) {
        phasePoses.get(phase).poseModel(actionCompletion, entity, ticks, yRotationOffset, xRotation, side);
    }

    
    
    public static class Builder<T extends Entity> {
        private final Map<StandEntityAction.Phase, IModelPose<T>> phasePoses = new EnumMap<>(StandEntityAction.Phase.class);
        
        public Builder<T> addPose(StandEntityAction.Phase phase, IModelPose<T> pose) {
            phasePoses.put(phase, pose);
            return this;
        }
        
        public StandActionAnimation<T> build(IModelPose<T> idlePose) {
            return new StandActionAnimation<T>(this, idlePose);
        }
    }
}
