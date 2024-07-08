package com.github.standobyte.jojo.client.playeranim.interfaces;

import com.github.standobyte.jojo.client.playeranim.IEntityAnimApplier;
import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;

public interface HamonMeditationPoseAnim extends BasicToggleAnim {
    
    default IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> initHamonMasterPose(HamonMasterModel model) {
        return IEntityAnimApplier.createDummy();
    }
    
    
    
    public static class NoPlayerAnimator extends BasicToggleAnim.NoPlayerAnimator implements HamonMeditationPoseAnim {}
}
