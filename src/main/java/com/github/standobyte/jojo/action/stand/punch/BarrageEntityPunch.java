package com.github.standobyte.jojo.action.stand.punch;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class BarrageEntityPunch extends StandEntityPunch {
    private int barrageHits = 0;

    public BarrageEntityPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        super(stand, target, dmgSource);
        this
        .damage(StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage(), stand.getPrecision()))
        .addCombo(0.005F)
        .reduceKnockback(0.1F)
        .setPunchSound(ModSounds.STAND_BARRAGE_ATTACK);
    }
    
    public BarrageEntityPunch barrageHits(StandEntity stand, int hits) {
        this.barrageHits = hits;
        damage(StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage(), stand.getPrecision()) * hits);
        return this;
    }
    
    @Override
    public boolean hit(StandEntity stand, StandEntityTask task) {
        if (stand.level.isClientSide()) return false;
        if (barrageHits > 0) {
            dmgSource.setBarrageHitsCount(barrageHits);
        }
        return super.hit(stand, task);
    }

    @Override
    protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
        if (hurt && dmgSource.getBarrageHitsCount() > 0) {
            addCombo *= dmgSource.getBarrageHitsCount();
        }
        super.afterAttack(stand, target, dmgSource, task, hurt, killed);
    }
}
