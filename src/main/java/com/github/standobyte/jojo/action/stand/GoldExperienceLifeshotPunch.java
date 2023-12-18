package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class GoldExperienceLifeshotPunch extends StandEntityHeavyAttack {

    public GoldExperienceLifeshotPunch(Builder builder) {
        super(builder);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        StandEntityPunch punchProperties = super.punchEntity(stand, target, dmgSource);
        return punchProperties
                .multiplyAddKnockback(1.25F);
    }
}
