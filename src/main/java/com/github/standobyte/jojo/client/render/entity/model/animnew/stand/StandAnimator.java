package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.entity.stand.StandPose;

import net.minecraft.entity.Entity;

public class StandAnimator<T extends Entity> {
    private final Animation idleAnim;
//    private final Animation idlePose0;
    private List<Animation> summonAnims = new ArrayList<>();
    private Map<StandPose, StandActionAnimation> actionAnims = new HashMap<>();
    
    public StandAnimator(Animation idleAnim) {
        this.idleAnim = idleAnim;
    }
    
    public StandAnimator<T> addSummonAnim(Animation anim) {
        // TODO
        return this;
    }
    
    public StandAnimator<T> addSummonAnimFromPose(Animation staticPose) {
        // TODO
        return this;
    }
    
    public StandAnimator<T> addActionAnim(StandPose standAction, StandActionAnimation anim) {
        // TODO
        return this;
    }
    
    
    public boolean animate(T standEntity) {
        return false;
    }
    
}
