package com.github.standobyte.jojo.power.impl.stand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;

public interface IStandPower extends IPower<IStandPower, StandType<?>> {
    Optional<StandInstance> getStandInstance();
    boolean giveStand(StandInstance standInstance, boolean newInstance);
    Optional<StandInstance> putOutStand();

    boolean usesStamina();
    float getStamina();
    float getMaxStamina();
    void addStamina(float amount, boolean sendToClient);
    boolean consumeStamina(float amount);
    boolean isStaminaInfinite();
    void setStamina(float amount);
    float getStaminaTickGain();

    ResolveCounter getResolveCounter();
    void setResolveCounter(ResolveCounter resolve);
    boolean usesResolve();
    float getResolve();
    float getMaxResolve();
    default float getResolveRatio() {
        if (!usesResolve()) {
            return 0;
        }
        return getResolve() / getMaxResolve();
    }
    int getResolveLevel();
    void setResolveLevel(int level, boolean fromEffect);
    int getMaxResolveLevel();
    float getResolveDmgReduction();
    float getPrevTickResolve();
    
    StandEffectsTracker getContinuousEffects();

    void skipProgression();
    void setProgressionSkipped();
    boolean wasProgressionSkipped();
    float getStatsDevelopment();
    
    boolean unlockAction(Action<IStandPower> action);
    void setLearningProgressPoints(Action<IStandPower> action, float progress, boolean clamp, boolean notLess);
    void addLearningProgressPoints(Action<IStandPower> action, float progress);
    ActionLearningProgressMap<IStandPower> clearActionLearning();
    
    void setStandManifestation(@Nullable IStandManifestation standManifestation);
    @Nullable
    IStandManifestation getStandManifestation();
    void toggleSummon();
    
    boolean hadStand();
    
    void onDash();
    
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
