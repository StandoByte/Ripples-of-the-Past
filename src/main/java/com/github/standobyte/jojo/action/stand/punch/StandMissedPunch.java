package com.github.standobyte.jojo.action.stand.punch;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public class StandMissedPunch implements IPunch {
    public final StandEntity stand;
    
    public StandMissedPunch(StandEntity stand) {
        this.stand = stand;
    }

    @Override
    public boolean doHit(StandEntityTask standTask) {
        return false;
    }

    @Override
    public boolean targetWasHit() {
        return false;
    }
    
    @Override
    public StandEntity getStand() {
        return stand;
    }

    @Override
    public SoundEvent getSound() {
        return null;
    }

    @Override
    public Vector3d getSoundPos() {
        return null;
    }
}
