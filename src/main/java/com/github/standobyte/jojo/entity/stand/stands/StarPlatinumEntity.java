package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class StarPlatinumEntity extends StandEntity {
    
    public StarPlatinumEntity(StandEntityType<StarPlatinumEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    protected StandAttackProperties standAttackProperties(PunchType punchType, Entity target, StandEntityAction action, 
            double strength, double precision, double attackRange, double distance, double knockback, int barrageHits) {
        StandAttackProperties attack = super.standAttackProperties(punchType, target, action, 
                strength, precision, attackRange, distance, knockback, barrageHits);
        switch (punchType) {
        case HEAVY_COMBO:
            attack
            .addKnockback(0.5F + getLastHeavyPunchCombo())
            .knockbackXRot(-60F)
            .disableBlocking((1 - (float) (distance / attackRange)) - 0.25F);
            break;
        default:
            break;
        }
        return attack;
    }
}
