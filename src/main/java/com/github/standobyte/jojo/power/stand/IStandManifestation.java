package com.github.standobyte.jojo.power.stand;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface IStandManifestation {
    void setUser(LivingEntity user);
    void setUserPower(IStandPower power);
    void syncWithTrackingOrUser(ServerPlayerEntity player);
}
