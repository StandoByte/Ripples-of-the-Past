package com.github.standobyte.jojo.util.damage;

import net.minecraft.util.DamageSource;

public class StandDamageSource extends DamageSource implements IStandDamageSource {
    
    public StandDamageSource(String msgId) {
        super(msgId);
    }
    
    StandDamageSource(DamageSource source) {
        super(source.msgId);
    }

    @Override
    public String toString() {
       return "StandDamageSource (" + msgId + ")";
    }
}
