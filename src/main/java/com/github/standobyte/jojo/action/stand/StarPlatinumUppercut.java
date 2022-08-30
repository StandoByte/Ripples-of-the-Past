package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class StarPlatinumUppercut extends StandEntityHeavyAttack {

    public StarPlatinumUppercut(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .addKnockback(0.5F + stand.getLastHeavyPunchCombo())
                .knockbackXRot(-60F)
                .disableBlocking((float) stand.getProximityRatio(target) - 0.25F);
    }

}
