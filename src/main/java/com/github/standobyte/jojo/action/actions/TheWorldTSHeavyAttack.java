package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class TheWorldTSHeavyAttack extends StandEntityHeavyAttack {
    public static final StandPose TS_PUNCH_POSE = new StandPose("TS_PUNCH");
    private final Supplier<StandEntityHeavyAttack> theWorldHeavyAttack;
    private final Supplier<TimeStopInstant> theWorldTimeStopBlink;

    public TheWorldTSHeavyAttack(StandEntityAction.Builder builder, 
            Supplier<StandEntityHeavyAttack> theWorldHeavyAttack, Supplier<TimeStopInstant> theWorldTimeStopBlink) {
        super(builder);
        this.theWorldHeavyAttack = theWorldHeavyAttack;
        this.theWorldTimeStopBlink = theWorldTimeStopBlink;
    }
    
    @Override
    public ActionTarget targetBeforePerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            return ActionTarget.fromRayTraceResult(stand.aimWithStandOrUser(stand.getMaxRange(), target));
        }
        return super.targetBeforePerform(world, user, power, target);
    }

    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return super.getStandWindupTicks(standPower, standEntity);
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return super.getStandActionTicks(standPower, standEntity);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        super.standPerform(world, standEntity, userPower, target);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, int ticks) {
        super.onTaskSet(world, standEntity, standPower, phase, ticks);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        super.standTickPerform(world, standEntity, ticks, userPower, target);
    }
    
    @Override
    public boolean useDeltaMovement(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
}
