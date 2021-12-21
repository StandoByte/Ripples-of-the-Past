package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class TheWorldRoadRoller extends StandEntityAction {

    public TheWorldRoadRoller(Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            RoadRollerEntity roadRoller = new RoadRollerEntity(world);
            roadRoller.copyPosition(user);
            world.addFreshEntity(roadRoller);
            user.startRiding(roadRoller);
        }
    }
    
    @Override
    public int getCooldown(IStandPower power, int ticksHeld) {
        if (power.isUserCreative()) {
            return 0;
        }
        return super.getCooldown(power, ticksHeld);
    }
}
