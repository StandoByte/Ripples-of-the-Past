package com.github.standobyte.jojo.action.player;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;

public interface IPlayerAction<T extends ContinuousActionInstance<T, P>, P extends IPower<P, ?>> {
    
    default void setPlayerAction(LivingEntity user, P power) {
        user.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            T action = createContinuousActionInstance(user, cap, power);
            cap.setContinuousAction(action);
        });
    }
    
    T createContinuousActionInstance(LivingEntity user, PlayerUtilCap userCap, P power);
    
    void playerTick(T continuousAction);
}
