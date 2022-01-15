package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float damage) {
        boolean dmgPhysical = ModDamageSources.hurtThroughInvulTicks(target, dmgSource, damage / 2);
        boolean dmgFire = ModDamageSources.hurtThroughInvulTicks(target, dmgSource.setIsFire(), damage / 2);
        return dmgPhysical || dmgFire;
    }
    
    @Override
    public boolean attackEntity(Entity target, boolean strongAttack, double attackDistance) {
        if (super.attackEntity(target, strongAttack, attackDistance)) {
            int seconds = 10;
            if (target instanceof StandEntity) {
                ((StandEntity) target).setFireFromStand(seconds);
            }
            else {
                target.setSecondsOnFire(seconds);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void playStandSummonSound() {
        if (!isArmsOnlyMode()) {
            super.playStandSummonSound();
        }
    }
}
