package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel.VisibilityMode;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.HandSide;

public class StandArmBarrageSwing<T extends StandEntity, M extends StandEntityModel<T>> extends ArmBarrageSwing<T, M> {

    public StandArmBarrageSwing(IBarrageAnimation<T, M> barrageAnim, 
            float ticks, float ticksMax, HandSide side, double maxOffset) {
        super(barrageAnim, ticks, ticksMax, side, maxOffset);
    }

    @Override
    protected void setArmOnlyModelVisibility(T entity, M model, HandSide side) {
        model.setVisibility(entity, side == HandSide.LEFT ? VisibilityMode.LEFT_ARM_ONLY : VisibilityMode.RIGHT_ARM_ONLY, false);
    }

}
