package com.github.standobyte.jojo.util.mc.damage;

public interface IModdedDamageSource {
    IModdedDamageSource setKnockbackReduction(float factor);
    float getKnockbackFactor();

    IModdedDamageSource setBypassInvulTicksInEvent();
    boolean bypassInvulTicks();

    IModdedDamageSource setPreventsDamagingArmor();
    boolean preventDamagingArmor();
}
