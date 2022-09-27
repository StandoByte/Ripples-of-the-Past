package com.github.standobyte.jojo.action.stand.punch;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public interface IPunch {
    boolean doHit(StandEntityTask task);
    boolean targetWasHit();
    
    StandEntity getStand();
    
    SoundEvent getSound();
    Vector3d getSoundPos();
    default boolean playSound() {
        return targetWasHit();
    }
    
    TargetType getType();
}
