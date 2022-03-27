package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class TheWorldEntity extends StandEntity {
    
    public TheWorldEntity(StandEntityType<TheWorldEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    protected StandAttackProperties standAttackProperties(PunchType punchType, Entity target, StandEntityAction action, 
            double strength, double precision, double attackRange, double distance, double knockback, int barrageHits) {
        StandAttackProperties attack = super.standAttackProperties(punchType, target, action, 
                strength, precision, attackRange, distance, knockback, barrageHits);
        
        switch (punchType) {
        case HEAVY_NO_COMBO:
            attack
            .armorPiercing((float) strength * 0.01F)
            .addKnockback(6);
            break;
        case HEAVY_COMBO:
            attack
            .addKnockback(4)
            .knockbackYRotDeg(60)
            .disableBlocking((1 - (float) (distance / attackRange)) - 0.5F)
            .sweepingAttack(0.5, 0, 0.5, attack.getDamage() * 0.5F);
            break;
        default:
            break;
        }
        return attack;
    }
}
