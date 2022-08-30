package com.github.standobyte.jojo.action.stand.punch;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;

import net.minecraft.block.BlockState;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class StandBlockPunch implements IPunch {
    public final StandEntity stand;
    public final BlockPos blockPos;
    public final BlockState blockState;
    private boolean targetHit;
    // FIXME !! punch sound
    protected Supplier<SoundEvent> punchSound = () -> null;
    
    public StandBlockPunch(StandEntity stand, BlockPos targetPos, BlockState blockState) {
        this.stand = stand;
        this.blockPos = targetPos;
        this.blockState = blockState;
    }

    @Override
    public boolean hit(StandEntity stand, StandEntityTask task) {
        if (stand.level.isClientSide()) return false;
        targetHit = stand.breakBlock(blockPos, blockState, true);
        return targetHit;
    }

    @Override
    public boolean targetWasHit() {
        return targetHit;
    }
    
    public StandBlockPunch setPunchSound(Supplier<SoundEvent> sound) {
        this.punchSound = sound;
        return this;
    }

    @Override
    public SoundEvent getSound() {
        return punchSound != null ? punchSound.get() : null;
    }
    
    @Override
    public Vector3d getSoundPos() {
        return Vector3d.atCenterOf(blockPos);
    }
}
