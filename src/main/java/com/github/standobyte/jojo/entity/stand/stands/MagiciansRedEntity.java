package com.github.standobyte.jojo.entity.stand.stands;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public boolean attackEntity(Entity target, PunchType punch, StandEntityAction action, 
            int barrageHits, @Nullable Consumer<StandAttackProperties> attackOverride) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> super.attackEntity(target, punch, action, barrageHits, attackOverride), 10, true);
    }
    
    @Override
    public void playStandSummonSound() {
        if (!isArmsOnlyMode()) {
            super.playStandSummonSound();
        }
    }
    
    @Override
    protected StandAttackProperties standAttackProperties(PunchType punchType, Entity target, StandEntityAction action, 
            double strength, double precision, double attackRange, double distance, double knockback, int barrageHits) {
        StandAttackProperties attack = super.standAttackProperties(punchType, target, action, 
                strength, precision, attackRange, distance, knockback, barrageHits);
        
        switch (punchType) {
        case HEAVY_COMBO:
            attack
            .addKnockback(4);
            break;
        default:
            break;
        }
        return attack;
    }
}
