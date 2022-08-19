package com.github.standobyte.jojo.action.stand.effect;

import net.minecraft.entity.LivingEntity;

// FIXME !! (blood cutter) the effect doesn't get saved
// FIXME !! (blood cutter) targets do not get updated on client if you unload their chunks and reload them
public class DriedBloodDrops extends StandEffectInstance {

    public DriedBloodDrops(StandEffectType<?> effectType) {
        super(effectType);
    }

    @Override
    protected void start() {}

    @Override
    protected void tickTarget(LivingEntity target) {}

    @Override
    protected void tick() {}

    @Override
    protected void stop() {}
    
    @Override
    protected boolean needsTarget() {
        return true;
    }
    
    @Override
    public boolean removeOnUserLogout() {
        return false;
    }

}
