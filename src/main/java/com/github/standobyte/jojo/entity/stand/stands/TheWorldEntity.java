package com.github.standobyte.jojo.entity.stand.stands;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class TheWorldEntity extends StandEntity {
    
    public TheWorldEntity(StandEntityType<TheWorldEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    protected StandAttackProperties standAttackProperties(PunchType punchType, Entity target, StandEntityAction action, @Nullable LivingEntity targetLiving, 
            double strength, double precision, double attackRange, double distance, double knockback, int barrageHits) {
        StandAttackProperties attack = super.standAttackProperties(punchType, target, action, targetLiving, 
                strength, precision, attackRange, distance, knockback, barrageHits);
        
        switch (punchType) {
        case HEAVY_NO_COMBO:
            attack.armorPiercing(attack.getArmorPiercing() * 2).addKnockback(6);
            break;
        case HEAVY_COMBO:
            attack.addKnockback(4).knockbackYRotDeg(60);
            break;
        default:
            break;
        }
        return attack;
    }
}
