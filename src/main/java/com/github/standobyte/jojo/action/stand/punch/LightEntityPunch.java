package com.github.standobyte.jojo.action.stand.punch;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class LightEntityPunch extends StandEntityPunch {
    
    public LightEntityPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        super(stand, target, dmgSource);
        this
        .damage(StandStatFormulas.getLightAttackDamage(stand.getAttackDamage()))
        .addKnockback(stand.guardCounter())
        .addCombo(0.15F)
        .parryTiming(stand.getComboMeter() == 0 ? StandStatFormulas.getParryTiming(stand.getPrecision()) : 0)
        .setPunchSound(ModSounds.STAND_LIGHT_ATTACK);
    }

}
