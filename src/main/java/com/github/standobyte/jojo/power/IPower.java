package com.github.standobyte.jojo.power;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.general.Container;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
    
    default int getColor() {
        return hasPower() ? getType().getColor() : -1;
    }
    default ITextComponent getName() {
        return hasPower() ? getType().getName() : StringTextComponent.EMPTY;
    }
    
    ActionsLayout<P> getActionsLayout();
    default ActionHotbarData<P> getActions(ActionType hotbar) {
        return getActionsLayout().getHotbar(hotbar);
    }
    
    boolean isActionOnCooldown(Action<?> action);
    float getCooldownRatio(Action<?> action, float partialTick);
    void setCooldownTimer(Action<?> action, int value);
    void updateCooldownTimer(Action<?> action, int value, int totalCooldown);
    void resetCooldowns();
    ActionCooldownTracker getCooldowns();

    @Nullable default Action<P> getAction(ActionType type, int index, boolean shift) {
        List<Action<P>> actions = getActions(type).getEnabled();
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        return getActionOnClick(actions.get(index), shift);
    }
    
    @Nullable default Action<P> getQuickAccessAction(boolean shift) {
        return getActionOnClick(getActionsLayout().getQuickAccessAction(), shift);
    }
    
    @Nullable Action<P> getActionOnClick(Action<P> actionInSlot, boolean shift);
    boolean clickAction(Action<P> action, boolean shift, ActionTarget target);
    ActionConditionResult checkRequirements(Action<P> action, Container<ActionTarget> targetContainer, boolean checkTargetType);
    ActionConditionResult checkTarget(Action<P> action, Container<ActionTarget> targetContainer);
    boolean canUsePower();
    
    default RayTraceResult clientHitResult(Entity cameraEntity, RayTraceResult mcHitResult) {
        return getType() != null ? getType().clientHitResult((P) this, cameraEntity, mcHitResult) : mcHitResult;
    }
    
    float getLearningProgressPoints(Action<P> action);
    float getLearningProgressRatio(Action<P> action);

    void setHeldAction(Action<P> action);
    @Nullable default Action<P> getHeldAction() {
        return getHeldAction(false);
    }
    @Nullable Action<P> getHeldAction(boolean checkRequirements);
    void refreshHeldActionTickState(boolean requirementsFulfilled);
    int getHeldActionTicks();
    void stopHeldAction(boolean shouldFire);

    void setMouseTarget(ActionTarget target);
    @Nullable ActionTarget getMouseTarget();
    boolean isTargetUpdateTick();
    
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

    default boolean onClickAction(ActionType type, int index, boolean shift, ActionTarget target, Optional<Action<?>> inputValidation) {
        return onClickAction(this.getAction(type, index, shift), shift, target, inputValidation);
    }
    
    default boolean onClickQuickAccess(boolean shift, ActionTarget target, Optional<Action<?>> inputValidation) {
        return onClickAction(this.getQuickAccessAction(shift), shift, target, inputValidation);
    }
    
    default boolean onClickAction(Action<P> action, boolean shift, ActionTarget target, Optional<Action<?>> inputValidation) {
        if (action != null && inputValidation.map(clientAction -> clientAction == action).orElse(true)) {
            return clickAction(action, shift, target);
        }
        return false;
    }

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
