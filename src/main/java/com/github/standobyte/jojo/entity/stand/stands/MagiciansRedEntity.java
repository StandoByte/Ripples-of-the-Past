package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket.StandSoundType;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void rangedAttackTick(int ticks, boolean shift) {
        MRFlameEntity flame = new MRFlameEntity(this, level);
        flame.setDamageFactor(rangeEfficiencyFactor());
        flame.shootFromRotation(this, xRot + (random.nextFloat() - 0.5F) * 10F, yRot + (random.nextFloat() - 0.5F) * 10F, 
                0, (float) getMeleeAttackRange() / 4F, 0.0F);
        level.addFreshEntity(flame);
        playSound(ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 0.5F, 0.3F + random.nextFloat() * 0.4F);
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
    public void playStandSound(StandSoundType soundType) {
        if (!isArmsOnlyMode()) {
            super.playStandSound(soundType);
        }
    }
}
