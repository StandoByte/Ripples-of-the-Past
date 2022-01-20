package com.github.standobyte.jojo.power;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
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
    void setCooldownTimer(Action<?> action, int value, int totalCooldown);
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
    ActionConditionResult checkRequirements(Action<P> action, ActionTarget target, boolean checkTargetType);
    ActionConditionResult checkTargetType(Action<P> action, ActionTarget target);
    boolean canUsePower();

    void setHeldAction(Action<?> action);
    default Action<P> getHeldAction() {
        return getHeldAction(false);
    }
    Action<P> getHeldAction(boolean checkRequirements);
    void refreshHeldActionTickState(boolean requirementsFulfilled);
    int getHeldActionTicks();
    void setHeldActionTarget(ActionTarget target);
    void stopHeldAction(boolean shouldFire);

    boolean canLeap();
    boolean isLeapUnlocked();
    float leapStrength();
    void onLeap();
    int getLeapCooldown();
    void setLeapCooldown(int cooldown);
    int getLeapCooldownPeriod();

    INBT writeNBT();
    void readNBT(CompoundNBT nbt);
    void onClone(P oldPower, boolean wasDeath, boolean configToKeep);
    void syncWithUserOnly();
    void syncWithTrackingOrUser(ServerPlayerEntity player);


    public static LazyOptional<? extends IPower<?, ?>> getPowerOptional(LivingEntity entity, PowerClassification classification) {
        return classification == PowerClassification.STAND ? IStandPower.getStandPowerOptional(entity) : INonStandPower.getNonStandPowerOptional(entity);
    }

    public static IPower<?, ?> getPlayerPower(PlayerEntity player, PowerClassification classification) {
        return classification == PowerClassification.STAND ? IStandPower.getPlayerStandPower(player) : INonStandPower.getPlayerNonStandPower(player);
    }
    
    public static void castAndGivePower(IPower<?, ?> power, IPowerType<?, ?> powerType, PowerClassification classification) { // FIXME get rid of this shit
        switch (classification) {
        case STAND:
            if (power instanceof IStandPower && powerType instanceof StandType) {
                ((IStandPower) power).givePower((StandType<?>) powerType);
            }
            break;
        case NON_STAND:
            if (power instanceof INonStandPower && powerType instanceof NonStandPowerType<?>) {
                ((INonStandPower) power).givePower((NonStandPowerType<?>) powerType);
            }
            break;
        }
    }
    
    public static enum PowerClassification {
        STAND,
        NON_STAND
    }
    
    public static enum ActionType {
        ATTACK,
        ABILITY
    }
}
