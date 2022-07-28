package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGGrapplingStringEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class HierophantGreenGrapple extends StandEntityAction {
    public static final StandPose GRAPPLE_POSE = new StandPose("HG_GRAPPLE");
    
    public HierophantGreenGrapple(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            HGGrapplingStringEntity string = new HGGrapplingStringEntity(world, standEntity, userPower);
            if (isShiftVariation()) {
                string.setBindEntities(true);
            }
            world.addFreshEntity(string);
        }
    }
    
    @Override
    public boolean standRetractsAfterTask(IStandPower standPower, StandEntity standEntity) {
    	return isShiftVariation();
    }
}
