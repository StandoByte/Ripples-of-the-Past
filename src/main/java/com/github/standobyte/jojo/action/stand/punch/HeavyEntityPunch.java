package com.github.standobyte.jojo.action.stand.punch;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class HeavyEntityPunch extends StandEntityPunch {
    
    public HeavyEntityPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        super(stand, target, dmgSource);
        double strength = stand.getAttackDamage();
        this
        .damage(StandStatFormulas.getHeavyAttackDamage(strength))
        .addKnockback(1 + (float) strength / 8)
        .setStandInvulTime(10)
        .setPunchSound(ModSounds.STAND_STRONG_ATTACK);
    }
}
