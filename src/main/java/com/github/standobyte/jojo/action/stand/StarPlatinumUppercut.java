package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class StarPlatinumUppercut extends StandEntityHeavyAttack {

    public StarPlatinumUppercut(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        StandEntityPunch punch = super.punchEntity(stand, target, dmgSource);
        double strength = stand.getAttackDamage();
        return punch
                .addKnockback(0.5F + (float) strength / 16 * stand.getLastHeavyFinisherValue())
                .knockbackXRot(-60F)
                .disableBlocking(1.0F);
    }

}
