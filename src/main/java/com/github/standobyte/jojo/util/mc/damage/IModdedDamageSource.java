package com.github.standobyte.jojo.util.mc.damage;

public interface IModdedDamageSource {
     StandEntityDamageSource setKnockbackReduction(float factor);
     float getKnockbackFactor();
     StandEntityDamageSource setBypassInvulTicksInEvent();
     boolean bypassInvulTicks();
}
