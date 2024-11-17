package com.github.standobyte.jojo.action.player;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;

public interface IPlayerAction<I extends ContinuousActionInstance<?, P>, P extends IPower<P, ?>> {
    
    default void setPlayerAction(LivingEntity user, P power) {
        user.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            I action = createContinuousActionInstance(user, cap, power);
            cap.setContinuousAction(action);
        });
    }
    
    I createContinuousActionInstance(LivingEntity user, PlayerUtilCap userCap, P power);
}
