package com.github.standobyte.jojo.power.impl.stand;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;

public interface IStandPower extends IPower<IStandPower, StandType<?>> {
    Optional<StandInstance> getStandInstance();
    boolean giveStandFromInstance(StandInstance standInstance, boolean standExistedInWorld);
    Optional<StandInstance> putOutStand();
    void setStandInstance(StandInstance standInstance);
    
    PreviousStandsSet getPreviousStandsSet();
    boolean hadAnyStand();
    StandArrowHandler getStandArrowHandler();
    
    boolean usesStamina();
    float getStamina();
    float getMaxStamina();
    void addStamina(float amount, boolean sendToClient);
    default boolean consumeStamina(float amount) { return consumeStamina(amount, false); }
    boolean consumeStamina(float amount, boolean ticking);
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
    void setResolveLevel(int level);
    int getMaxResolveLevel();
    float getResolveDmgReduction();
    float getPrevTickResolve();

    boolean willSoulSpawn();
    void clSetSoulSpawnFlag(boolean flag);
    boolean spawnSoulOnDeath();
    
    StandEffectsTracker getContinuousEffects();

    void skipProgression();
    void setProgressionSkipped();
    boolean wasProgressionSkipped();
    float getStatsDevelopment();

    boolean unlockAction(StandAction action);
    void setLearningProgressPoints(StandAction action, float progress);
    void setLearningFromPacket(StandActionLearningPacket packet);
    float getLearningProgressPoints(StandAction action);
    void addLearningProgressPoints(StandAction action, float progress);
    Iterable<StandAction> getAllUnlockedActions();
    default boolean hasUnlockedMatching(Predicate<StandAction> actionPredicate) {
        return StreamSupport.stream(getAllUnlockedActions().spliterator(), false)
                .anyMatch(actionPredicate);
    }
    void fullStandClear();
    
    void setStandManifestation(@Nullable IStandManifestation standManifestation);
    @Nullable
    IStandManifestation getStandManifestation();
    void toggleSummon();
    
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
