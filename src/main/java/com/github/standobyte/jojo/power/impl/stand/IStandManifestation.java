package com.github.standobyte.jojo.power.impl.stand;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface IStandManifestation {
    void setUserAndPower(LivingEntity user, IStandPower power);
    void syncWithTrackingOrUser(ServerPlayerEntity player);
}
