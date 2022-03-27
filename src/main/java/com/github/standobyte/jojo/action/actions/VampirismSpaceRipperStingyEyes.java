package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SpaceRipperStingyEyesEntity;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

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

    // FIXME (!!!!!!!) cooldown
    @Override
    public int getCooldown(INonStandPower power, int ticksHeld) {
        return super.getCooldown(power, ticksHeld) * ticksHeld / this.getHoldDurationMax(power);
    }

}
