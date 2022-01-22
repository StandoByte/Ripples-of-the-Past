package com.github.standobyte.jojo.util.damage;

import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

public interface IStandDamageSource {
    
    IStandPower getStandPower();
    
    public static void copyDamageSourceProperties(DamageSource src, DamageSource dest) {
        if (src.isBypassArmor()) {
            dest.bypassArmor();
        }
        if (src.isBypassInvul()) {
            dest.bypassInvul();
        }
        if (src.isBypassMagic()) {
            dest.bypassMagic();
        }
        if (src.isFire()) {
            dest.setIsFire();
        }
        if (src.isProjectile()) {
            dest.setProjectile();
        }
        if (src.isMagic()) {
            dest.setMagic();
        }
        if (src.isExplosion()) {
            dest.setExplosion();
        }
        if (src instanceof EntityDamageSource && ((EntityDamageSource) src).isThorns() && dest.getEntity() != null) {
            ((EntityDamageSource) dest).setThorns();
        }
    }
}
