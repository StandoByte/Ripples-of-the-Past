package com.github.standobyte.jojo.power.stand;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SkippedStandProgressionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionsClearLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeStandInstancePacket;
import com.github.standobyte.jojo.power.PowerBaseImpl;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.LegacyUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class StandPower extends PowerBaseImpl<IStandPower, StandType<?>> implements IStandPower {
    private Optional<StandInstance> standInstance = Optional.empty();
    private int tier = 0;
    @Nullable
    private IStandManifestation standManifestation = null;
    private float stamina;
    
    private final ResolveCounter resolveCounter;
    private boolean skippedProgression;
    private boolean givenByDisc;
    
    private StandEffectsTracker continuousEffects = new StandEffectsTracker(this);
    
    private ActionLearningProgressMap<IStandPower> actionLearningProgressMap = new ActionLearningProgressMap<>();
    
    public StandPower(LivingEntity user) {
        super(user);
        resolveCounter = new ResolveCounter(this);
    }
    
    @Override
    public PowerClassification getPowerClassification() {
        return PowerClassification.STAND;
    }
    
    @Override
    public Optional<StandInstance> getStandInstance() {
        return standInstance;
    }
    
    @Override
    public StandType<?> getType() {
        return getStandInstance().map(StandInstance::getType).orElse(null);
    }

    @Override
    public boolean hasPower() {
        return getStandInstance().isPresent();
    }

    @Override
    public boolean givePower(StandType<?> standType) {
        return giveStand(new StandInstance(standType), true);
    }
    
    @Override
    public boolean giveStand(StandInstance standInstance, boolean newInstance) {
        if (standInstance == null || (user == null || !user.level.isClientSide()) && !canGetPower(standInstance.getType())) {
            return false;
        }

        setStandInstance(standInstance);
        StandType<?> standType = standInstance.getType();
        onNewPowerGiven(standType);
        if (newInstance) {
            serverPlayerUser.ifPresent(player -> {
                SaveFileUtilCapProvider.getSaveFileCap(player).addPlayerStand(standType);
            });
        }
        return true;
    }
    
    private void setStandInstance(StandInstance standInstance) {
        this.standInstance = Optional.ofNullable(standInstance);
        onPowerSet(this.standInstance.map(StandInstance::getType).orElse(null));
    }

    @Override
    protected void onNewPowerGiven(StandType<?> standType) {
        super.onNewPowerGiven(standType);
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClientsTrackingAndSelf(new TrTypeStandInstancePacket(
                    player.getId(), getStandInstance().get(), resolveCounter.getResolveLevel()), player);
        });
        setStamina(getMaxStamina() * 0.5F);
        if (user != null && (JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).skipStandProgression.get()
                || user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild)) {
            skipProgression(standType);
        }
        else {
            standType.unlockNewActions(this);
        }
    }
    
    @Override
    protected void onPowerSet(StandType<?> standType) {
        super.onPowerSet(standType);
        if (usesStamina()) {
            stamina = isUserCreative() ? getMaxStamina() : 0;
        }
        if (usesResolve()) {
            resolveCounter.onStandAcquired(standType);
        }
        if (standType != null) {
            tier = Math.max(tier, standType.getTier());
        }
        if (user != null && !user.level.isClientSide()) {
            continuousEffects.onStandChanged(user);
        }
    }

    @Override
    public boolean clear() {
        return clear(true);
    }

    @Override
    public Optional<StandInstance> putOutStand() {
        Optional<StandInstance> standInstance = getStandInstance();
        return clear(false) ? standInstance : null;
    }

    private boolean clear(boolean countTaken) {
        StandType<?> standType = getType();
        if (super.clear()) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(TrTypeStandInstancePacket.noStand(player.getId()), player);
            });
            if (isActive()) {
                standType.forceUnsummon(user, this);
            }
            setStandInstance(null);
            stamina = 0;
            resolveCounter.reset();
            skippedProgression = false;
            givenByDisc = false;
            if (countTaken) {
                serverPlayerUser.ifPresent(player -> {
                    SaveFileUtilCapProvider.getSaveFileCap(player).removePlayerStand(standType);
                });
            }
            if (user != null) {
                continuousEffects.onStandChanged(user);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void setGivenByDisc() {
        givenByDisc = true;
    }
    
    @Override
    public boolean wasGivenByDisc() {
        return givenByDisc;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (hasPower()) {
            tickStamina();
            tickResolve();
            if (user != null) {
                standInstance.ifPresent(stand -> stand.tick(this, user, user.level));
            }
            continuousEffects.tick();
        }
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
        return StandUtil.standIgnoresStaminaDebuff(this);
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
                PacketManager.sendToClientsTrackingAndSelf(new TrStaminaPacket(user.getId(), getStamina()), player);
            });
        }
    }
    
    private void tickStamina() {
        if (usesStamina()) {
            addStamina(getStaminaTickGain(), false);
        }
    }
    
    @Override
    public float getStaminaTickGain() {
        float staminaRegen = getType().getStaminaRegen(this);
        staminaRegen *= INonStandPower.getNonStandPowerOptional(getUser()).map(power -> {
            if (power.hasPower()) {
                return power.getType().getStaminaRegenFactor(power, this);
            }
            return 1F;
        }).orElse(1F);
        
        if (getUser() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) getUser();
            if (player.getFoodData().getFoodLevel() > 17) {
                staminaRegen *= 1.25F;
            }
        }
        return staminaRegen * getStaminaDurabilityModifier();
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
    public StandEffectsTracker getContinuousEffects() {
        return continuousEffects;
    }
    

    @Override
    public void skipProgression(StandType<?> standType) {
    	setProgressionSkipped();
        resolveCounter.setResolveLevel(getMaxResolveLevel());
        if (standType != null) {
            Stream.concat(
                    Arrays.stream(standType.getAttacks()), 
                    Arrays.stream(standType.getAbilities()))
            .flatMap(action -> action.hasShiftVariation() && action.getShiftVariationIfPresent() instanceof StandAction
                    ? Stream.of(action, (StandAction) action.getShiftVariationIfPresent()) : Stream.of(action))
            .forEach(action -> {
                actionLearningProgressMap.setLearningProgressPoints(action, action.getMaxTrainingPoints(this), this);
            });
        }
    }
    
    @Override
    public void setProgressionSkipped() {
        this.skippedProgression = true;
    }
    
    @Override
    public boolean wasProgressionSkipped() {
        return skippedProgression;
    }
    
    @Override
    public float getStatsDevelopment() {
        return usesResolve() ? (float) getResolveLevel() / (float) getMaxResolveLevel() : 0;
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
        return actionLearningProgressMap.getLearningProgressPoints(action, this, true);
    }

    @Override
    public void setLearningProgressPoints(Action<IStandPower> action, float points, boolean clamp, boolean notLess) {
        if (clamp) {
            points = MathHelper.clamp(points, 0, action.getMaxTrainingPoints(this));
        }
        if (notLess) {
            points = Math.max(points, actionLearningProgressMap.getLearningProgressPoints(action, this, false));
        }
        float pts = points;
        if (actionLearningProgressMap.setLearningProgressPoints(action, points, this)) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new StandActionLearningPacket(action, pts, true), player);
            });
            
            if (user != null && !user.level.isClientSide()) {
                action.onTrainingPoints(this, actionLearningProgressMap.getLearningProgressPoints(action, this, false));
                if (actionLearningProgressMap.getLearningProgressPoints(action, this, true)
                        == action.getMaxTrainingPoints(this)) {
                    action.onMaxTraining(this);
                }
            }
        }
    }

    @Override
    public void addLearningProgressPoints(Action<IStandPower> action, float points) {
        if (user != null && user.hasEffect(ModEffects.RESOLVE.get())) {
            points *= 4;
        }
        setLearningProgressPoints(action, getLearningProgressPoints(action) + points, true, true);
    }
    
    @Override
    public ActionLearningProgressMap<IStandPower> clearActionLearning() {
        ActionLearningProgressMap<IStandPower> previousMap = actionLearningProgressMap;
        this.actionLearningProgressMap = new ActionLearningProgressMap<>();
        resolveCounter.clearLevels();
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new StandActionsClearLearningPacket(), player);
        });
        return previousMap;
    }
    
    @Override
    public void setStandManifestation(IStandManifestation standManifestation) {
        this.standManifestation = standManifestation;
        if (standManifestation != null) {
            standManifestation.setUserAndPower(getUser(), this);
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
    public int getUserTier() {
        return tier;
    }
    
//    @Override // TODO Stand Sealing effect
//    public boolean canUsePower() {
//        return super.canUsePower() && !user.hasEffect(ModEffects.STAND_SEALING.get());
//    }
    
    @Override
    public boolean isLeapUnlocked() {
        return getStandInstance().map(stand -> stand.hasPart(StandPart.LEGS)).orElse(false) && leapStrength() >= 1.5;
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
    public boolean canLeap() {
    	if (super.canLeap()) {
    		return !(standManifestation instanceof StandEntity && ((StandEntity) standManifestation).getCurrentTask().isPresent());
    	}
        return false;
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
        consumeStamina(250);
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
        standInstance.ifPresent(stand -> cnbt.put("StandInstance", stand.writeNBT()));
        if (usesStamina()) {
            cnbt.putFloat("Stamina", stamina);
        }
        if (usesResolve()) {
            cnbt.put("Resolve", resolveCounter.writeNBT());
        }
        cnbt.putBoolean("Skipped", skippedProgression);
        cnbt.putBoolean("Disc", givenByDisc);
        cnbt.put("ActionLearning", actionLearningProgressMap.toNBT());
        cnbt.put("Effects", continuousEffects.toNBT());
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        StandInstance standInstance = nbt.contains("StandInstance", JojoModUtil.getNbtId(CompoundNBT.class))
                ? StandInstance.fromNBT(nbt.getCompound("StandInstance"))
                        : LegacyUtil.readOldStandCapType(nbt).orElse(null);
        setStandInstance(standInstance);
            
        if (usesStamina()) {
            stamina = nbt.getFloat("Stamina");
        }
        if (usesResolve()) {
            resolveCounter.readNbt(nbt.getCompound("Resolve"));
        }
        skippedProgression = nbt.getBoolean("Skipped");
        givenByDisc = nbt.getBoolean("Disc");
        if (nbt.contains("ActionLearning", JojoModUtil.getNbtId(CompoundNBT.class))) {
            actionLearningProgressMap.fromNBT(nbt.getCompound("ActionLearning"));
        }
        if (nbt.contains("Effects", JojoModUtil.getNbtId(CompoundNBT.class))) {
            continuousEffects.fromNBT(nbt.getCompound("Effects"));
        }
        super.readNBT(nbt);
    }
    
    @Override
    protected void keepPower(IStandPower oldPower, boolean wasDeath) {
        super.keepPower(oldPower, wasDeath);
        oldPower.getStandInstance().ifPresent(stand -> this.setStandInstance(stand));
        this.setResolveCounter(oldPower.getResolveCounter());
        if (wasDeath) {
            this.resolveCounter.alwaysResetOnDeath();
        }
        this.skippedProgression = oldPower.wasProgressionSkipped();
        this.givenByDisc = oldPower.wasGivenByDisc();
        this.actionLearningProgressMap = ((StandPower) oldPower).actionLearningProgressMap; // FIXME can i remove this cast?
        this.continuousEffects = oldPower.getContinuousEffects();
        this.stamina = getMaxStamina();
    }
    
    @Override
    public void syncWithUserOnly() {
        super.syncWithUserOnly();
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                if (usesResolve()) {
                    resolveCounter.syncWithUser(player);
                }
            }
            actionLearningProgressMap.forEach((action, progress) -> {
                PacketManager.sendToClient(new StandActionLearningPacket(action, progress, false), player);
            });
            if (skippedProgression) {
                PacketManager.sendToClient(new SkippedStandProgressionPacket(), player);
            }
            continuousEffects.syncWithUserOnly(player);
        });
    }
    
    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        super.syncWithTrackingOrUser(player);
        if (hasPower()) {
            if (user != null) {
                PacketManager.sendToClient(new TrTypeStandInstancePacket(user.getId(), getStandInstance().get(), resolveCounter.getResolveLevel()), player);
            }
            if (usesStamina()) {
                PacketManager.sendToClient(new TrStaminaPacket(user.getId(), stamina), player);
            }
            if (standManifestation != null) {
                standManifestation.syncWithTrackingOrUser(player);
            }
        }
        continuousEffects.syncWithTrackingOrUser(player);
    }
}
