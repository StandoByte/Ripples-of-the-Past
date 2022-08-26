package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class CrazyDiamondLeaveObject extends StandEntityAction {

    public CrazyDiamondLeaveObject(Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
    }
}
