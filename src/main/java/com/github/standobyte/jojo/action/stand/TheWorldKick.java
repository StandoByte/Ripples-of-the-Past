package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class TheWorldKick extends StandEntityHeavyAttack {

    public TheWorldKick(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        StandEntityPunch kick = super.punchEntity(stand, target, dmgSource);
        return kick
                .addKnockback(3)
                .knockbackYRotDeg(60)
                .disableBlocking((float) stand.getProximityRatio(target) - 0.25F)
                .sweepingAttack(0.5, 0, 0.5, kick.getDamage() * 0.5F);
    }
}
