package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class TheWorldHeavyPunch extends StandEntityHeavyAttack {

    public TheWorldHeavyPunch(Builder builder) {
        super(builder);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .armorPiercing((float) stand.getAttackDamage() * 0.01F)
                .addKnockback(6);
    }

}
