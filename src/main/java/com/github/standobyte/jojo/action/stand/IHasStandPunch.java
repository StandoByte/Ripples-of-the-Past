package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.IPunch;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

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
    
    default void playPunchSound(IPunch punch, TargetType punchType, boolean canPlay, boolean playAlways) {
        if (canPlay && (playAlways || punch.playSound())) {
            SoundEvent punchSound = punch.getSound();
            if (punchSound != null) {
                Vector3d soundPos = punch.getSoundPos();
                if (soundPos != null) {
                    punch.getStand().playSound(punchSound, 1.0F, 1.0F, null, soundPos);
                }
            }
        }
    }
}
