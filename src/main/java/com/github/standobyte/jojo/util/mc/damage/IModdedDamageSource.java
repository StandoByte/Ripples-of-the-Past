package com.github.standobyte.jojo.util.mc.damage;

public interface IModdedDamageSource {
    IModdedDamageSource setKnockbackReduction(float factor);
    float getKnockbackFactor();
    
    IModdedDamageSource setStackKnockback();
    boolean doesStackKnockback();

    IModdedDamageSource setBypassInvulTicksInEvent();
    boolean bypassInvulTicks();

    IModdedDamageSource setPreventDamagingArmor();
    boolean preventsDamagingArmor();
    
    boolean canHurtStands();
}
