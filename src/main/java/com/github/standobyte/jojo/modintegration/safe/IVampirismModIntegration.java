package com.github.standobyte.jojo.modintegration.safe;

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
