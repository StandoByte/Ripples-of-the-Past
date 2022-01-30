package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class StarPlatinumStarFinger extends StandEntityAction {

    public StarPlatinumStarFinger(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            SPStarFingerEntity starFinger = new SPStarFingerEntity(world, standEntity);
            starFinger.setDamageFactor((float) standEntity.getRangeEfficiency());
            world.addFreshEntity(starFinger);
        }
    }
}
