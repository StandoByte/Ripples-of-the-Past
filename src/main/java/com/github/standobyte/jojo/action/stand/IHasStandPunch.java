package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface IHasStandPunch {
    
    default StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return new StandEntityPunch(stand, target, dmgSource);
    }
    
    default StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state) {
        return new StandBlockPunch(stand, pos, state);
    }
    
    default StandMissedPunch punchMissed(StandEntity stand) {
        return new StandMissedPunch(stand);
    }
}
