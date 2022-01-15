package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/* FIXME (stats)
 *   hold ticks: durability
 *   damage: strength, precision
 *   speed: speed
 *   
 *   replace cd with recovery ticks
 */
public class StandEntityMeleeBarrage extends StandEntityAction {

    public StandEntityMeleeBarrage(StandEntityAction.Builder builder) {
        super(builder.autoSummonMode(AutoSummonMode.ARMS).holdType().userMovementFactor(0.3F).defaultStandOffsetFromUser());
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackMelee() ? 
                ActionConditionResult.NEGATIVE
                : super.checkSpecificConditions(user, performer, power, target);
    }

    @Override
    public int getCooldown(IStandPower power, int ticksHeld) {
        return MathHelper.floor((float) (getCooldownValue() * ticksHeld) / (float) getHoldDurationMax(power) + 0.5F);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            double attackSpeed = standEntity.getAttackSpeed();
            int extraTickSwings = (int) (attackSpeed / 20D);
            for (int i = 0; i < extraTickSwings; i++) {
                standEntity.swingAlternateHands();
                standEntity.punch(false);
            }
            
            if (standEntity.barragePunchDelayed) {
                standEntity.barragePunchDelayed = false;
                standEntity.swingAlternateHands();
                standEntity.punch(false);
            }
            else {
                double sp2 = attackSpeed % 20D;
                if (sp2 > 0) {
                    double ticksInterval = 20 / sp2;
                    int intTicksInterval = (int) ticksInterval;
                    if ((getStandActionTicks(userPower, standEntity) - ticks + standEntity.barrageDelayedPunches) % intTicksInterval == 0) {
                        double delayProb = ticksInterval - intTicksInterval;
                        if (standEntity.getRandom().nextDouble() < delayProb) {
                            standEntity.barragePunchDelayed = true;
                            standEntity.barrageDelayedPunches++;
                        }
                        else {
                            standEntity.swingAlternateHands();
                            standEntity.punch(false);
                        }
                    }
                }
            }
        }
    }
}
