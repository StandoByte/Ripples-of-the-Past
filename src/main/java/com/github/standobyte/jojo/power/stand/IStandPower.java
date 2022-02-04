package com.github.standobyte.jojo.power.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.LazyOptional;

public interface IStandPower extends IPower<IStandPower, StandType<?>> {
    public static final int MAX_EXP = 1000;
    public static final float MAX_RESOLVE = 1000;
    
    boolean usesStamina();
    float getStamina();
    float getMaxStamina();
    void addStamina(float amount, boolean sendToClient);
    boolean consumeStamina(float amount);
    void setStamina(float amount);

    boolean usesResolve();
    float getResolve();
    float getMaxResolve();
    default float getResolveRatio() {
        return getResolve() / getMaxResolve();
    }
    void addResolve(float amount);
    void setResolve(float amount, int noDecayTicks);
    int getNoResolveDecayTicks();
    int getResolveLevel();
    int getMaxResolveLevel();
    void setResolveLevel(int level);
    float getResolveLimit();
    void addResolveLimit(float amount);
    void setResolveLimit(float amount, int noDecayTicks);
    float getResolveDmgReduction();
    void addResolveOnAttack(LivingEntity target, float damageAmount);
    void addResolveOnTakingDamage(DamageSource damageSource, float damageAmount);
    
    void skipProgression();
    boolean wasProgressionSkipped();
    float getStatsDevelopment();
    
    @Deprecated
    int getXp();
    @Deprecated
    void setXp(int xp);
    
    boolean unlockAction(Action<IStandPower> action);
    float getLearningProgress(Action<IStandPower> action);
    void setLearningProgress(Action<IStandPower> action, float progress);
    void addLearningProgress(Action<IStandPower> action, float progress);
    
    void setStandManifestation(@Nullable IStandManifestation standManifestation);
    @Nullable
    IStandManifestation getStandManifestation();
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
