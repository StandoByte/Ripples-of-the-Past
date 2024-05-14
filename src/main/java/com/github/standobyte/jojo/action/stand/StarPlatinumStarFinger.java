package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.World;

public class StarPlatinumStarFinger extends StandEntityAction {

    public StarPlatinumStarFinger(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            SPStarFingerEntity starFinger = new SPStarFingerEntity(world, standEntity);
            starFinger.setLifeSpan(getStandActionTicks(userPower, standEntity));
            starFinger.withStandSkin(standEntity.getStandSkin());
            standEntity.addProjectile(starFinger);
        }
    }
}
