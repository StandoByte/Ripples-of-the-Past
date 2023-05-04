package com.github.standobyte.jojo.action.player;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;

public class BaseContinuousActionInstance<P extends IPower<P, ?>> extends ContinuousActionInstance<BaseContinuousActionInstance<P>, P> {

    public BaseContinuousActionInstance(LivingEntity user, PlayerUtilCap userCap, P playerPower,
            IPlayerAction<BaseContinuousActionInstance<P>, P> action) {
        super(user, userCap, playerPower, action);
    }

    @Override
    protected BaseContinuousActionInstance<P> getThis() {
        return this;
    }

}
