package com.github.standobyte.jojo.action.stand.punch;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class TheWorldTSHeavyPunch extends HeavyEntityPunch {

    public TheWorldTSHeavyPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        super(stand, target, dmgSource);
        this
        .damage(StandStatFormulas.getHeavyAttackDamage(stand.getAttackDamage()))
        .addKnockback(4)
        .disableBlocking(1.0F)
        .setStandInvulTime(10)
        .setPunchSound(ModSounds.STAND_STRONG_ATTACK);
    }

    @Override
    protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
        if (killed) {
            LivingEntity user = stand.getUser();
            if (user != null && stand.distanceToSqr(user) > 16) {
                JojoModUtil.sayVoiceLine(user, ModSounds.DIO_THIS_IS_THE_WORLD.get());
            }
        }
    }

}
