package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGGrapplingStringEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class HierophantGreenGrapple extends StandEntityAction {
    public static final StandPose GRAPPLE_POSE = new StandPose("HG_GRAPPLE");
    
    public HierophantGreenGrapple(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            HGGrapplingStringEntity string = new HGGrapplingStringEntity(world, standEntity, userPower);
            if (isShiftVariation()) {
                string.setBindEntities(true);
            }
            world.addFreshEntity(string);
        }
    }
}
