package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotHeavyAttack extends StandEntityHeavyAttack {

    public SilverChariotHeavyAttack(Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        if (stand instanceof SilverChariotEntity && !((SilverChariotEntity) stand).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return standEntity.willHeavyPunchCombo() ? 
                Math.max(super.getStandWindupTicks(standPower, standEntity) - MathHelper.ceil(getStandActionTicks(standPower, standEntity) / 2F), 0)
                : 0;
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return standEntity.isHeavyComboPunching() ? 
                3
                : super.getStandWindupTicks(standPower, standEntity);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (standEntity.isHeavyComboPunching()) {
            double reach = standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
            world.getEntities(standEntity, standEntity.getBoundingBox().inflate(reach, 0, reach), 
                    e -> !e.isSpectator() && e.isPickable() && standEntity.canHarm(e)).forEach(targetEntity -> {
                        Vector3d standLookVec = standEntity.getLookAngle();
                        Vector3d targetVec = targetEntity.position().subtract(standEntity.position()).normalize();
                        double cos = standLookVec.dot(targetVec);
                        if (cos > -0.5) {
                            standEntity.attackEntity(targetEntity, PunchType.HEAVY_COMBO, this, 1, attack -> {
                                if (cos < 0) {
                                    attack.damage(attack.getDamage() * 0.6667F);
                                }
                                if (cos < 0.7071) {
                                    attack.damage(attack.getDamage() * 0.75F);
                                }
                                attack.addKnockback(1);
                            });
                        }
                    });
        }
        else {
            // FIXME (!!!!) (SC) thrusting stab
        }
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!standEntity.isHeavyComboPunching()) {
            // FIXME (!!!!) (SC) thrusting stab
        }
    }
}
