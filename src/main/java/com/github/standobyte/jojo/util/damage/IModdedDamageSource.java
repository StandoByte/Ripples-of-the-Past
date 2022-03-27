package com.github.standobyte.jojo.util.damage;

public interface IModdedDamageSource {
     StandEntityDamageSource setKnockbackReduction(float factor);
     float getKnockbackFactor();
     StandEntityDamageSource setBypassInvulTicksInEvent();
     boolean bypassInvulTicks();
}
