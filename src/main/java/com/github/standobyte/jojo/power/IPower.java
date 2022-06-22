package com.github.standobyte.jojo.power;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTargetContainer;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.LazyOptional;

public interface IPower<P extends IPower<P, T>, T extends IPowerType<P, T>> {
    PowerClassification getPowerClassification();
    boolean hasPower();
    boolean givePower(T type);
    boolean clear();
    T getType();
    LivingEntity getUser();
    boolean isUserCreative();
    void tick();
    boolean isActive();

    List<Action<P>> getAttacks();
    List<Action<P>> getAbilities();
    
    default List<Action<P>> getActions(ActionType type) {
        return type == ActionType.ATTACK ? getAttacks() : getAbilities();
    }

    boolean isActionOnCooldown(Action<?> action);
    float getCooldownRatio(Action<?> action, float partialTick);
    void setCooldownTimer(Action<?> action, int value);
    void updateCooldownTimer(Action<?> action, int value, int totalCooldown);
    ActionCooldownTracker getCooldowns();

    @Nullable Action<P> getAction(ActionType type, int index, boolean shift);
    boolean onClickAction(Action<P> action, boolean shift, ActionTarget target);
    default boolean onClickAction(ActionType type, int index, boolean shift, ActionTarget target) {
        Action<P> action = this.getAction(type, index, shift);
        if (action != null) {
            return onClickAction(action, shift, target);
        }
        return false;
    }
    ActionConditionResult checkRequirements(Action<P> action, ActionTargetContainer targetContainer, boolean checkTargetType);
    ActionConditionResult checkTargetType(Action<P> action, ActionTargetContainer targetContainer);
    boolean canUsePower();
    
    default RayTraceResult clientHitResult(Entity cameraEntity, RayTraceResult mcHitResult) {
        return getType() != null ? getType().clientHitResult((P) this, cameraEntity, mcHitResult) : mcHitResult;
    }
    
    float getLearningProgressPoints(Action<P> action);
    float getLearningProgressRatio(Action<P> action);

    void setHeldAction(Action<P> action);
    default Action<P> getHeldAction() {
        return getHeldAction(false);
    }
    Action<P> getHeldAction(boolean checkRequirements);
    void refreshHeldActionTickState(boolean requirementsFulfilled);
    int getHeldActionTicks();
    void setHeldActionTarget(ActionTarget target);
    void stopHeldAction(boolean shouldFire);

    void onUserGettingAttacked(DamageSource dmgSource, float dmgAmount);
    float getTargetResolveMultiplier(IStandPower attackingStand);
    
    boolean canLeap();
    boolean isLeapUnlocked();
    float leapStrength();
    void onLeap();
    int getLeapCooldown();
    void setLeapCooldown(int cooldown);
    int getLeapCooldownPeriod();

    INBT writeNBT();
    void readNBT(CompoundNBT nbt);
    void onClone(P oldPower, boolean wasDeath);
    void syncWithUserOnly();
    void syncWithTrackingOrUser(ServerPlayerEntity player);


    public static LazyOptional<? extends IPower<?, ?>> getPowerOptional(LivingEntity entity, PowerClassification classification) {
        return classification == PowerClassification.STAND ? IStandPower.getStandPowerOptional(entity) : INonStandPower.getNonStandPowerOptional(entity);
    }

    public static IPower<?, ?> getPlayerPower(PlayerEntity player, PowerClassification classification) {
        return classification == PowerClassification.STAND ? IStandPower.getPlayerStandPower(player) : INonStandPower.getPlayerNonStandPower(player);
    }
    
    public static enum PowerClassification {
        STAND(IStandPower.class),
        NON_STAND(INonStandPower.class);
        
        private final Class<? extends IPower<?, ?>> powerClass;
        
        private PowerClassification(Class<? extends IPower<?, ?>> powerClass) {
            this.powerClass = powerClass;
        }
        
        public Class<? extends IPower<?, ?>> getPowerClass() {
            return powerClass;
        }
    }
    
    public static enum ActionType {
        ATTACK,
        ABILITY
    }
}
