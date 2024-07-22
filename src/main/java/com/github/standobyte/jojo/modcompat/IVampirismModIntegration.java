package com.github.standobyte.jojo.modcompat;

import net.minecraft.entity.LivingEntity;

public interface IVampirismModIntegration {
    
    boolean isEntityVampire(LivingEntity entity);
    
    
    public static class Dummy implements IVampirismModIntegration {

        @Override
        public boolean isEntityVampire(LivingEntity entity) {
            return false;
        }
    }
}
