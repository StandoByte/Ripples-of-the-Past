package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SpaceRipperStingyEyesEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class VampirismSpaceRipperStingyEyes extends VampirismAction {

    public VampirismSpaceRipperStingyEyes(NonStandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            world.addFreshEntity(new SpaceRipperStingyEyesEntity(world, user, true));
            world.addFreshEntity(new SpaceRipperStingyEyesEntity(world, user, false));
        }
    }

    @Override
    public int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return cooldownFromHoldDuration(super.getCooldownAdditional(power, ticksHeld), power, ticksHeld);
    }
    
    @Override
    protected int maxCuringStage() {
        return 1;
    }

}
