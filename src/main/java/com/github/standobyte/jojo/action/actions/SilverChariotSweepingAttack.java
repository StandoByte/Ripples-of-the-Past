package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotSweepingAttack extends StandEntityComboHeavyAttack {

    public SilverChariotSweepingAttack(StandEntityHeavyAttack.Builder builder) {
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
        return Math.max(super.getStandWindupTicks(standPower, standEntity) - getStandActionTicks(standPower, standEntity) / 2, 0);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            double reach = standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
            world.getEntities(standEntity, standEntity.getBoundingBox().inflate(reach, 0, reach), 
                    e -> !e.isSpectator() && e.isPickable() && standEntity.canHarm(e)).forEach(targetEntity -> {
                        Vector3d standLookVec = standEntity.getLookAngle();
                        Vector3d targetVec = targetEntity.position().subtract(standEntity.position()).normalize();
                        double cos = standLookVec.dot(targetVec);
                        if (cos > -0.5) {
                            standEntity.attackEntity(targetEntity, PunchType.HEAVY_COMBO, this, 1, attack -> {
                                if (cos < 0.5) {
                                    attack.damage(attack.getDamage() * 0.5F);
                                }
                                attack.addKnockback(1);
                            });
                        }
                    });
        }
    }
}
