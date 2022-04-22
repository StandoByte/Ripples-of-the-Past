package com.github.standobyte.jojo.power.stand;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SkippedStandProgressionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandActionLearningClearPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandActionLearningPacket;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.PowerBaseImpl;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class StandPower extends PowerBaseImpl<IStandPower, StandType<?>> implements IStandPower {
    private int tier = 0;
    @Nullable
    private IStandManifestation standManifestation = null;
    private float stamina;
    
    private final ResolveCounter resolveCounter;
    @Deprecated
    private int xp = 0;
    private boolean skippedProgression;
    
    private ActionLearningProgressMap<IStandPower> actionLearningProgressMap = new ActionLearningProgressMap<>();
    
    
    public StandPower(LivingEntity user) {
        super(user);
        resolveCounter = new ResolveCounter(this, serverPlayerUser);
    }

    @Override
    public boolean givePower(StandType<?> standType) {
        if (super.givePower(standType)) {
            serverPlayerUser.ifPresent(player -> {
                SaveFileUtilCapProvider.getSaveFileCap(player).addPlayerStand(standType);
            });
            standType.unlockNewActions(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean clear() {
        StandType<?> standType = getType();
        if (super.clear()) {
            if (isActive()) {
                standType.forceUnsummon(user, this);
            }
            type = null;
            stamina = 0;
            resolveCounter.reset();
            xp = 0;
            skippedProgression = false;
            serverPlayerUser.ifPresent(player -> {
                SaveFileUtilCapProvider.getSaveFileCap(player).removePlayerStand(standType);
            });
            return true;
        }
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (hasPower()) {
            tickStamina();
            tickResolve();
        }
    }
    
    @Override
    public PowerClassification getPowerClassification() {
        return PowerClassification.STAND;
    }
    
    @Override
    protected void afterTypeInit(StandType<?> standType) {
        attacks = Arrays.asList(standType.getAttacks());
        abilities = Arrays.asList(standType.getAbilities());
        if (user != null && (JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).skipStandProgression.get()
                || user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild)) {
            skipProgression(standType);
        }
        if (usesStamina()) {
            stamina = isUserCreative() ? getMaxStamina() : 0;
        }
        if (usesResolve()) {
            resolveCounter.onStandAcquired();
        }
        tier = Math.max(tier, standType.getTier());
    }
    
    @Override
    public boolean isActive() {
        return hasPower() && getType().isStandSummoned(user, this);
    }


    @Override
    public boolean usesStamina() {
        return hasPower() && getType().usesStamina();
    }
    
    @Override
    public float getStamina() {
        return isStaminaInfinite() ? getMaxStamina() : stamina;
    }

    @Override
    public float getMaxStamina() {
        if (!usesStamina()) {
            return 0;
        }
        float maxAmount = getType().getMaxStamina(this);
        maxAmount *= INonStandPower.getNonStandPowerOptional(getUser()).map(power -> {
            if (power.hasPower()) {
                return power.getType().getMaxStaminaFactor(power, this);
            }
            return 1F;
        }).orElse(1F);
        return maxAmount * getStaminaDurabilityModifier();
    }
    
    @Override
    public void addStamina(float amount, boolean sendToClient) {
        setStamina(MathHelper.clamp(this.stamina + amount, 0, getMaxStamina()), sendToClient);
    }

    @Override
    public boolean consumeStamina(float amount) {
        if (isStaminaInfinite()) {
            return true;
        }
        if (getStamina() >= amount) {
            setStamina(this.stamina - amount);
            return true;
        }
        setStamina(0);
        return false;
    }
    
    @Override
    public boolean isStaminaInfinite() {
        return user == null || isUserCreative() || 
                !JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).standStamina.get();
    }

    @Override
    public void setStamina(float amount) {
        setStamina(amount, true);
    }
    
    private void setStamina(float amount, boolean sendToClient) {
        amount = MathHelper.clamp(amount, 0, getMaxStamina());
        boolean send = sendToClient && this.stamina != amount;
        this.stamina = amount;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncStaminaPacket(getStamina()), player);
            });
        }
    }
    
    private void tickStamina() {
        if (usesStamina()) {
            float staminaRegen = getType().getStaminaRegen(this);
            staminaRegen *= INonStandPower.getNonStandPowerOptional(getUser()).map(power -> {
                if (power.hasPower()) {
                    return power.getType().getStaminaRegenFactor(power, this);
                }
                return 1F;
            }).orElse(1F);
            
            if (getUser() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) getUser();
//                if (getStamina() < getMaxStamina()) {
//                    player.causeFoodExhaustion(0.005F);
//                }
                if (player.getFoodData().getFoodLevel() > 17) {
                    staminaRegen *= 1.25F;
                }
            }
            
            addStamina(staminaRegen * getStaminaDurabilityModifier(), false);
        }
    }
    
    private float getStaminaDurabilityModifier() {
        if (hasPower()) {
            StandStats stats = getType().getStats();
            return StandStatFormulas.getStaminaMultiplier(stats.getBaseDurability() + stats.getDevDurability(getStatsDevelopment()));
        }
        return 1F;
    }
    
    

    @Override
    public ResolveCounter getResolveCounter() {
        return resolveCounter;
    }
    
    @Override
    public void setResolveCounter(ResolveCounter resolve) {
        this.resolveCounter.clone(resolve);
    }
    
    @Override
    public boolean usesResolve() {
        return hasPower() && getType().usesResolve();
    }
    
    @Override
    public float getResolve() {
        if (!usesResolve()) {
            return 0;
        }
        return resolveCounter.getResolveValue();
    }

    @Override
    public float getMaxResolve() {
        if (!usesResolve()) {
            return 0;
        }
        return resolveCounter.getMaxResolveValue();
    }

    @Override
    public int getResolveLevel() {
        if (!usesResolve()) {
            return 0;
        }
        return resolveCounter.getResolveLevel();
    }
    
    @Override
    public void setResolveLevel(int level) {
        if (usesResolve()) {
            resolveCounter.setResolveLevel(level);
            if (!user.level.isClientSide() && hasPower()) {
                getType().onNewResolveLevel(this);
                if (level >= getType().getMaxResolveLevel()) {
                    serverPlayerUser.ifPresent(player -> {
                        ModCriteriaTriggers.STAND_MAX.get().trigger(player);
                    });
                }
            }
        }
    }

    @Override
    public int getMaxResolveLevel() {
        if (!usesResolve()) {
            return 0;
        }
        return getType().getMaxResolveLevel();
    }

    @Override
    public float getResolveDmgReduction() {
        if (INonStandPower.getNonStandPowerOptional(user).map(power -> 
        power.getType() == ModNonStandPowers.VAMPIRISM.get()).orElse(false)) {
            return 0;
        }
        if (user.hasEffect(ModEffects.RESOLVE.get())) {
            return ResolveCounter.RESOLVE_DMG_REDUCTION;
        }
        if (usesResolve()) {
            return getResolveRatio() * ResolveCounter.RESOLVE_DMG_REDUCTION;
        }
        return 0;
    }

    private void tickResolve() {
        if (usesResolve()) {
            resolveCounter.tick();
        }
    }
    
    
    @Override
    public void skipProgression(@Nullable StandType<?> standType) {
        this.skippedProgression = true;
        resolveCounter.setResolveLevel(getMaxResolveLevel());
        if (standType != null) {
            Stream.concat(
                    Arrays.stream(standType.getAttacks()), 
                    Arrays.stream(standType.getAbilities()))
            .forEach(action -> {
                actionLearningProgressMap.setLearningProgressPoints(action, action.getMaxTrainingPoints(this), this);
                if (action.hasShiftVariation()) {
                    Action<IStandPower> shiftAction = action.getShiftVariationIfPresent();
                    actionLearningProgressMap.setLearningProgressPoints(shiftAction, shiftAction.getMaxTrainingPoints(this), this);
                }
            });
        }
    }
    
    @Override
    public boolean wasProgressionSkipped() {
        return skippedProgression;
    }
    
    @Override
    public float getStatsDevelopment() {
        return usesResolve() ? (float) getResolveLevel() / (float) getMaxResolveLevel() : 0;
    }
    

    @Deprecated
    @Override
    public int getXp() {
        return xp;
    }

    @Deprecated
    @Override
    public void setXp(int xp) {
        xp = MathHelper.clamp(xp, 0, MAX_EXP);
        this.xp = xp;
    }

    @Override
    public boolean unlockAction(Action<IStandPower> action) {
        if (!action.isUnlocked(this)) {
            setLearningProgressPoints(action, 
                    isUserCreative() || !action.isTrained() ? 
                            action.getMaxTrainingPoints(this)
                            : 0F, false, false);
            return true;
        }
        return false;
    }

    @Override
    public float getLearningProgressPoints(Action<IStandPower> action) {
        return actionLearningProgressMap.getLearningProgressPoints(action, this);
    }

    @Override
    public void setLearningProgressPoints(Action<IStandPower> action, float points, boolean clamp, boolean notLess) {
        if (clamp) {
            points = MathHelper.clamp(points, 0, action.getMaxTrainingPoints(this));
        }
        if (notLess) {
            points = Math.max(points, getLearningProgressPoints(action));
        }
        float pts = points;
        if (actionLearningProgressMap.setLearningProgressPoints(action, points, this)) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncStandActionLearningPacket(action, pts, true), player);
            });
            if (!user.level.isClientSide() && 
                    actionLearningProgressMap.getLearningProgressPoints(action, this) == 
                    action.getMaxTrainingPoints(this)) {
                action.onMaxTraining(this);
            }
        }
    }

    @Override
    public void addLearningProgressPoints(Action<IStandPower> action, float points) {
        setLearningProgressPoints(action, getLearningProgressPoints(action) + points, true, false);
    }
    
    @Override
    public ActionLearningProgressMap<IStandPower> clearActionLearning() {
        ActionLearningProgressMap<IStandPower> previousMap = actionLearningProgressMap;
        this.actionLearningProgressMap = new ActionLearningProgressMap<>();
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new SyncStandActionLearningClearPacket(), player);
        });
        return previousMap;
    }
    
    @Override
    public void setStandManifestation(IStandManifestation standManifestation) {
        this.standManifestation = standManifestation;
        if (standManifestation != null) {
            standManifestation.setUser(getUser());
            standManifestation.setUserPower(this);
        }
    }
    
    @Override
    public IStandManifestation getStandManifestation() {
        return standManifestation;
    }
    
    @Override
    public void toggleSummon() {
        if (hasPower()) {
            getType().toggleSummon(this);
        }
    }
    
    @Override
    public boolean isStandRemotelyControlled() {
        return standManifestation instanceof StandEntity ? ((StandEntity) standManifestation).isManuallyControlled() : false;
    }
    
    @Override
    public int getTier() {
        return tier;
    }
    
//    @Override // TODO Stand Sealing effect
//    public boolean canUsePower() {
//        return super.canUsePower() && !user.hasEffect(ModEffects.STAND_SEALING.get());
//    }
    
    @Override
    public boolean isLeapUnlocked() {
        return leapStrength() >= 1.5;
    }
    
    @Override
    public float leapStrength() {
        if (standManifestation instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) standManifestation;
            if (!standEntity.isArmsOnlyMode() && standEntity.isFollowingUser()) {
                return standEntity.getLeapStrength();
            }
        }
        return 0;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        if (standManifestation instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) standManifestation;
            if (!standEntity.isArmsOnlyMode() && standEntity.isFollowingUser()) {
                double speed = standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED);
                return StandStatFormulas.leapCooldown(speed);
            }
        }
        return 0;
    }
    
    @Override
    public void onLeap() {
        super.onLeap();
        consumeStamina(200);
    }
    
    @Override
    public void onDash() {
        setLeapCooldown(getDashCooldownPeriod());
        consumeStamina(25);
    }
    
    private int getDashCooldownPeriod() {
        if (standManifestation instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) standManifestation;
            if (!standEntity.isArmsOnlyMode() && standEntity.isFollowingUser()) {
                double speed = standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED);
                return StandStatFormulas.dashCooldown(speed);
            }
        }
        return 0;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = super.writeNBT();
        cnbt.putString("StandType", ModStandTypes.Registry.getKeyAsString(getType()));
        if (usesStamina()) {
            cnbt.putFloat("Stamina", stamina);
        }
        if (usesResolve()) {
            cnbt.put("Resolve", resolveCounter.writeNBT());
        }
        cnbt.putInt("Xp", getXp());
        cnbt.putBoolean("Skipped", skippedProgression);
        actionLearningProgressMap.writeToNbt(cnbt);
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        String standName = nbt.getString("StandType");
        if (standName != IPowerType.NO_POWER_NAME) {
            StandType<?> stand = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standName));
            if (stand != null) {
                setType(stand);
                if (nbt.contains("Exp")) {
                    xp = nbt.getInt("Exp");
                    // FIXME (!!) add unlocked actions from v0.1
                }
                else {
                    xp = nbt.getInt("Xp");
                }
            }
        }
        if (usesStamina()) {
            stamina = nbt.getFloat("Stamina");
        }
        if (usesResolve()) {
            resolveCounter.readNbt(nbt.getCompound("Resolve"));
        }
        skippedProgression = nbt.getBoolean("Skipped");
        actionLearningProgressMap.readFromNbt(nbt);
        super.readNBT(nbt);
    }
    
    @Override
    protected void keepPower(IStandPower oldPower, boolean wasDeath) {
        super.keepPower(oldPower, wasDeath);
        this.xp = oldPower.getXp();
        this.stamina = oldPower.getStamina();
        this.setResolveCounter(oldPower.getResolveCounter());
        if (wasDeath) {
            this.resolveCounter.alwaysResetOnDeath();
        }
        this.skippedProgression = oldPower.wasProgressionSkipped();
        this.actionLearningProgressMap = ((StandPower) oldPower).actionLearningProgressMap; // FIXME can i remove this cast?
    }
    
    @Override
    public void syncWithUserOnly() {
        super.syncWithUserOnly();
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                if (usesStamina()) {
                    PacketManager.sendToClient(new SyncStaminaPacket(stamina), player);
                }
                if (usesResolve()) {
                    resolveCounter.syncWithUser(player);
                }
            }
            actionLearningProgressMap.forEach((action, progress) -> {
                PacketManager.sendToClient(new SyncStandActionLearningPacket(action, progress, false), player);
            });
            if (skippedProgression) {
                PacketManager.sendToClient(new SkippedStandProgressionPacket(), player);
            }
        });
    }
    
    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        super.syncWithTrackingOrUser(player);
        if (hasPower()) {
            if (standManifestation != null) {
                standManifestation.syncWithTrackingOrUser(player);
            }
        }
    }
}
