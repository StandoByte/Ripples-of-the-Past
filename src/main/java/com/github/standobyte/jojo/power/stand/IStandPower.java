package com.github.standobyte.jojo.power.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;

public interface IStandPower extends IPower<IStandPower, StandType<?>> {
    public static final int MAX_EXP = 1000;
    
    boolean usesStamina();
    float getStamina();
    float getMaxStamina();
    void addStamina(float amount);
    void consumeStamina(float amount);
    void setStamina(float amount);

    boolean usesResolve();
    float getResolve();
    float getMaxResolve();
    int getNoResolveDecayTicks();
    void addResolve(float amount);
    void setResolve(float amount, int noDecayTicks);
    boolean isInResolveMode();
    
    @Deprecated
    int getXp();
    @Deprecated
    void setXp(int xp);
    
    void setStandManifestation(@Nullable IStandManifestation standManifestation);
    @Nullable
    IStandManifestation getStandManifestation(); // FIXME stand manifestation shit
    void toggleSummon();
    
    int getTier();
    
    public static LazyOptional<IStandPower> getStandPowerOptional(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return entity.getCapability(StandCapProvider.STAND_CAP);
        }
        if (entity instanceof IMobStandUser) {
            return LazyOptional.of(() -> ((IMobStandUser) entity).getStandPower());
        }
        return LazyOptional.empty();
    }
    
    public static IStandPower getPlayerStandPower(PlayerEntity player) {
        return getStandPowerOptional(player).orElseThrow(() -> new IllegalStateException("Player's stand power capability is empty."));
    }
}
