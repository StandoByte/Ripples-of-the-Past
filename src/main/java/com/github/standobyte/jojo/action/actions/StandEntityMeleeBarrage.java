package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class StandEntityMeleeBarrage extends StandEntityAction {

    public StandEntityMeleeBarrage(StandEntityAction.Builder builder) {
        super(builder.standAutoSummonMode(AutoSummonMode.ARMS).holdType().standUserSlowDownFactor(0.3F).defaultStandOffsetFromUser());
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }

    @Override
    public int getCooldown(IStandPower power, int ticksHeld) {
        return MathHelper.floor((float) (getCooldownValue() * ticksHeld) / (float) getHoldDurationMax(power) + 0.5F);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        int hitsThisTick = 0;
        int hitsPerSecond = StandStatFormulas.getBarrageHitsPerSecond(standEntity.getAttackSpeed());
        int extraTickSwings = hitsPerSecond / 20;
        for (int i = 0; i < extraTickSwings; i++) {
            swing(standEntity);
            hitsThisTick++;
        }
        hitsPerSecond -= extraTickSwings * 20;
        
        if (standEntity.barragePunchDelayed) {
            standEntity.barragePunchDelayed = false;
            hitsThisTick++;
        }
        else if (hitsPerSecond > 0) {
            double ticksInterval = 20D / hitsPerSecond;
            int intTicksInterval = (int) ticksInterval;
            if ((getStandActionTicks(userPower, standEntity) - ticks + standEntity.barrageDelayedPunches) % intTicksInterval == 0) {
                if (!world.isClientSide()) {
                    double delayProb = ticksInterval - intTicksInterval;
                    if (standEntity.getRandom().nextDouble() < delayProb) {
                        standEntity.barragePunchDelayed = true;
                        standEntity.barrageDelayedPunches++;
                    }
                    else {
                        hitsThisTick++;
                    }
                }
                swing(standEntity);
            }
        }
        if (!world.isClientSide()) {
            standEntity.barrageTickPunches(target, this, hitsThisTick);
            // FIXME tick stamina cost in builder (for highlighting)
            userPower.consumeStamina(5F);
        }
    }
    
    private void swing(StandEntity standEntity) {
        if (standEntity.level.isClientSide()) {
            standEntity.swing(standEntity.alternateHands());
        }
    }
    
    @Override
    public boolean isCombatAction() {
        return true;
    }
    
    @Override
    public int getHoldDurationMax(IStandPower standPower) {
        if (standPower.getStandManifestation() instanceof StandEntity) {
            return StandStatFormulas.getBarrageMaxDuration(((StandEntity) standPower.getStandManifestation()).getDurability());
        }
        return 0;
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getBarrageRecovery(standEntity.getSpeed());
    }
}
