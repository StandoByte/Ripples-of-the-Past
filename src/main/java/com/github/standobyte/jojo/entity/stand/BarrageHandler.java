package com.github.standobyte.jojo.entity.stand;

import java.util.Optional;

import net.minecraft.entity.Entity;

public class BarrageHandler {
//    private final StandEntity stand;
    private boolean hasDelayedHit = false;
    private int delayedHits = 0;
    boolean accumulateTickParry = false;
    int parryCount = 0;
    Optional<Entity> clashOpponent = Optional.empty();
    
    public BarrageHandler(StandEntity stand) {
//        this.stand = stand;
    }
    
    public void delayHit() {
        hasDelayedHit = true;
        delayedHits++;
    }
    
    public boolean popDelayedHit() {
        if (hasDelayedHit) {
            hasDelayedHit = false;
            return true;
        }
        return false;
    }
    
    public int getHitsDelayed() {
        return delayedHits;
    }
    
    public void addParryCount(int hits) {
        if (!accumulateTickParry) {
            accumulateTickParry = true;
            parryCount = hits + 1;
        }
        else {
            parryCount += hits;
        }
    }
    
    void tick() {
        accumulateTickParry = false;
    }
    
    public void reset() {
        hasDelayedHit = false;
        delayedHits = 0;
        accumulateTickParry = false;
        parryCount = 0;
        clashOpponent = Optional.empty();
    }
}
