package com.github.standobyte.jojo.power.impl.nonstand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public abstract class TypeSpecificData {
    protected INonStandPower power;
    protected Optional<ServerPlayerEntity> serverPlayer;
    
    public void setPower(INonStandPower power) {
        this.power = power;
        LivingEntity user = power.getUser();
        this.serverPlayer = user instanceof ServerPlayerEntity ? Optional.of((ServerPlayerEntity) user) : Optional.empty();
    }
    
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower powerData) {
        return true;
    }
    
    public void onPowerGiven(@Nullable NonStandPowerType<?> oldType, @Nullable TypeSpecificData oldData) {}
    
    public abstract CompoundNBT writeNBT();
    public abstract void readNBT(CompoundNBT nbt);
    
    public abstract void syncWithUserOnly(ServerPlayerEntity user);
    public abstract void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity);
}
