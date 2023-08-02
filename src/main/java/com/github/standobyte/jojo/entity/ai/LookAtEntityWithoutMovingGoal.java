package com.github.standobyte.jojo.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.LookAtWithoutMovingGoal;
import net.minecraft.entity.player.PlayerEntity;

public class LookAtEntityWithoutMovingGoal extends LookAtWithoutMovingGoal {
    
    public LookAtEntityWithoutMovingGoal(MobEntity mob, LivingEntity lookAtEntity) {
        super(mob, PlayerEntity.class, 8, 1);
        this.lookAt = lookAtEntity;
    }
    
    @Override
    public boolean canUse() {
        return lookAt != null;
    }
}
