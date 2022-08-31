package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import com.github.standobyte.jojo.client.model.pose.anim.IActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.HandSide;

public interface IBarrageAnimation<T extends StandEntity> extends IActionAnimation<T> {

    public void animateSwing(T entity, float loopCompletion, HandSide side, float yRotationOffset, float xRotation);
}
