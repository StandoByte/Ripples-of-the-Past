package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float damage) {
        boolean dmgFire = DamageUtil.hurtThroughInvulTicks(target, dmgSource.setIsFire(), damage / 2);
        boolean dmgPhysical = DamageUtil.hurtThroughInvulTicks(target, dmgSource, damage / 2);
        return dmgPhysical || dmgFire;
    }
    
    @Override
    public boolean attackEntity(Entity target, PunchType punch, StandEntityAction action, int barrageHits) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> super.attackEntity(target, punch, action, barrageHits), 10, true);
    }
    
    @Override
    public void playStandSummonSound() {
        if (!isArmsOnlyMode()) {
            super.playStandSummonSound();
        }
    }
}
