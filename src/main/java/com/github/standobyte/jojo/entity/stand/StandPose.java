package com.github.standobyte.jojo.entity.stand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.StandEntityLightAttack;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandActionAnimation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandPoseData;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;

public class StandPose {
    private static final Map<String, StandPose> ALL_POSES = new HashMap<>();
    private final String name;
    public final boolean armsObstructView;
    
    public StandPose(String name, boolean armsObstructView) {
        this.name = name;
        this.armsObstructView = armsObstructView;
        if (ALL_POSES.containsKey(name)) {
            JojoMod.getLogger().warn("Stand pose {} is already present.", name);
        }
    }
    
    public StandPose(String name) {
        this(name, false);
    }
    
    public String getName() {
        return name;
    }
    
    public StandActionAnimation getAnim(List<StandActionAnimation> variants, @Nullable StandEntity standEntity) {
        return variants.get(0);
    }
    
    public <T extends StandEntity> void applyAnim(@Nullable T entity, StandEntityModel<T> model, 
            StandActionAnimation anim, float yRotOffsetDeg, float xRotDeg, StandPoseData poseData) {
        anim.poseStand(entity, model, yRotOffsetDeg, xRotDeg, poseData);
    }
    
    public static final StandPose IDLE = new StandPose("idle");
    public static final StandPose SUMMON = new StandPose("summon") {
        @Override
        public StandActionAnimation getAnim(List<StandActionAnimation> variants, StandEntity standEntity) {
            return standEntity != null ? variants.get(standEntity.getSummonPoseRandomByte() % variants.size()) : super.getAnim(variants, standEntity);
        }
    };
    public static final StandPose BLOCK = new StandPose("block");
    public static final StandPose LIGHT_ATTACK = StandEntityLightAttack.STAND_POSE;
    public static final StandPose HEAVY_ATTACK = new StandPose("heavyPunch");
    @Deprecated public static final StandPose HEAVY_ATTACK_FINISHER = new StandPose("finisherPunch");
    @Deprecated public static final StandPose RANGED_ATTACK = new StandPose("rangedAttack");
    public static final StandPose BARRAGE = new StandPose("barrage");
}
