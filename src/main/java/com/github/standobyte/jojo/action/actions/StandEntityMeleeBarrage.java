package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.task.MeleeAttackTask;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class StandEntityMeleeBarrage extends StandEntityAction {

    public StandEntityMeleeBarrage(StandEntityAction.Builder builder) {
        super(builder);
        this.doNotAutoSummonStand = true;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkConditions(user, performer, power, target);
    }
    
    @Override
    public void onStartedHolding(World world, LivingEntity user, IPower<?> power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            StandEntity stand;
            if (power.isActive()) {
                stand = (StandEntity) getPerformer(user, power);
            }
            else {
                IStandPower standPower = (IStandPower) power;
                ((EntityStandType) standPower.getType()).summon(user, standPower, entity -> {
                    entity.setArmsOnlyMode();
                }, true);
                stand = (StandEntity) standPower.getStandManifestation();
            }
            stand.setTask(new MeleeAttackTask(stand, true));
        }
    }
    
    @Override
    public void onStoppedHolding(World world, LivingEntity user, IPower<?> power, int ticksHeld) {
        if (!world.isClientSide() && power.isActive()) {
            ((StandEntity) ((IStandPower) power).getStandManifestation()).clearTask();
        }
    }

    @Override
    public int getCooldown(IPower<?> power, int ticksHeld) {
        return MathHelper.floor((float) (getCooldownValue() * ticksHeld) / (float) getHoldDurationMax() + 0.5F);
    }
}
