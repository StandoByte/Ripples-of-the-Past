package com.github.standobyte.jojo.power.impl.stand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SkippedStandProgressionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SoulSpawnPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandFullClearPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeStandInstancePacket;
import com.github.standobyte.jojo.power.impl.PowerBaseImpl;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandActionLearningProgress.StandActionLearningEntry;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.util.mod.LegacyUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;

public class StandPower extends PowerBaseImpl<IStandPower, StandType<?>> implements IStandPower {
    private Optional<StandInstance> standInstance = Optional.empty();
    private Optional<ResourceLocation> invalidReadStandId = Optional.empty();;
    private Optional<CompoundNBT> invalidReadStandNbt = Optional.empty();;
    
    private boolean hadStand = false;
    private PreviousStandsSet previousStands = new PreviousStandsSet();
    private StandArrowHandler standArrowHandler = new StandArrowHandler();
    
    @Nullable
    private IStandManifestation standManifestation = null;
    private float stamina;
    
    private final ResolveCounter resolveCounter;
    private boolean skippedProgression;
    
    private StandEffectsTracker continuousEffects = new StandEffectsTracker(this);
    
    private StandActionLearningProgress actionLearningProgressMap = new StandActionLearningProgress();
    
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
        return giveStandFromInstance(new StandInstance(standType), false);
    }
    
    @Override
    public boolean giveStandFromInstance(StandInstance standInstance, boolean standExistedInWorld) {
        if (standInstance == null) {
            return false;
        }
        
        StandType<?> standType = standInstance.getType();
        if ((user == null || !user.level.isClientSide()) && !canGetPower(standType)) {
            return false;
        }
        
        setStandInstance(standInstance);
        onNewPowerGiven(standType);
        if (!standExistedInWorld) {
            serverPlayerUser.ifPresent(player -> {
                SaveFileUtilCapProvider.getSaveFileCap(player).addPlayerStand(standType);
            });
        }
        return true;
    }
    
    
    @Override
    public void setStandInstance(StandInstance standInstance) {
        this.standInstance = Optional.ofNullable(standInstance);
        clUpdateHud();
    }

    @Override
    protected void onNewPowerGiven(StandType<?> standType) {
        super.onNewPowerGiven(standType);
        
        if (usesStamina()) {
            stamina = isUserCreative() ? getMaxStamina() : 0;
        }
        if (usesResolve()) {
            resolveCounter.onStandAcquired(standType);
        }
        if (standType != null) {
            hadStand = true;
        }
        if (user != null && !user.level.isClientSide()) {
            continuousEffects.onStandChanged(user);
            PacketManager.sendToClientsTrackingAndSelf(new TrTypeStandInstancePacket(
                    user.getId(), getStandInstance().get(), resolveCounter.getResolveLevel()), user);
        }
        
        if (user != null && !user.level.isClientSide()) {
            setStamina(getMaxStamina() * 0.5F);
            if (playerSkipsActionTraining()) {
                skipProgression();
            }
            else {
                standType.unlockNewActions(this);
            }
        }
        
        previousStands.addStand(standType, user);
    }
    
    @Override
    public boolean clear() {
        return clear(true);
    }

    @Override
    public Optional<StandInstance> putOutStand() {
        Optional<StandInstance> standInstance = getStandInstance();
        return clear(false) ? standInstance : Optional.empty();
    }

    private boolean clear(boolean countTaken) {
        StandType<?> standType = getType();
        if (super.clear()) {
            if (user != null && !user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(TrTypeStandInstancePacket.noStand(user.getId()), user);
            }
            if (isActive()) {
                standType.forceUnsummon(user, this);
            }
            setStandInstance(null);
            stamina = 0;
            resolveCounter.reset();
            skippedProgression = false;
            if (countTaken) {
                serverPlayerUser.ifPresent(player -> {
                    SaveFileUtilCapProvider.getSaveFileCap(player).removePlayerStand(standType);
                });
            }
            if (user != null) {
                continuousEffects.onStandChanged(user);
            }
            clUpdateHud();
            return true;
        }
        return false;
    }
    
    @Override
    public PreviousStandsSet getPreviousStandsSet() {
        return previousStands;
    }
    
    @Override
    public StandArrowHandler getStandArrowHandler() {
        return standArrowHandler;
    }
    
    @Override
    public boolean hadAnyStand() {
        return hadStand;
    }
    
    @Override
    public ITextComponent getName() {
        return hasPower() ? getStandInstance().map(stand -> stand.getName())
                .orElse(StringTextComponent.EMPTY) : StringTextComponent.EMPTY;
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
        if (!user.level.isClientSide()) {
            standArrowHandler.tick(user);
            tickSoulCheck();
        }
        else if (user == ClientUtil.getClientPlayer()) {
            if (getStamina() < getMaxStamina() * 0.5F && !StandUtil.standIgnoresStaminaDebuff(this)) {
                BarsRenderer.getBarEffects(BarType.STAMINA).triggerRedHighlight(999999);
            }
            else {
                BarsRenderer.getBarEffects(BarType.STAMINA).resetRedHighlight();
            }
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

    private float staminaAddNextTick = 0;
    @Override
    public boolean consumeStamina(float amount, boolean ticking) {
        if (isStaminaInfinite()) {
            return true;
        }
        if (getStamina() >= amount) {
            if (ticking) {
                staminaAddNextTick -= amount;
            }
            else if (!user.level.isClientSide()) {
                setStamina(this.stamina - amount);
            }
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
        if (this.stamina != amount) {
            this.stamina = amount;
            if (sendToClient && user != null && !user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrStaminaPacket(user.getId(), getStamina()), user);
            }
        }
    }
    
    private void tickStamina() {
        if (usesStamina()) {
            addStamina(getStaminaTickGain() + staminaAddNextTick, false);
            staminaAddNextTick = 0;
        }
    }
    
    @Override
    public float getStaminaTickGain() {
        float staminaRegen = getType().getStaminaRegen(this);
        if (staminaRegen > 0) {
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
        power.getType() == ModPowers.VAMPIRISM.get()).orElse(false)) {
            return 0;
        }
        if (user.hasEffect(ModStatusEffects.RESOLVE.get())) {
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
    public float getPrevTickResolve() {
        if (!usesResolve()) {
            return 0;
        }
        return resolveCounter.getPrevTickResolveValue();
    }
    
    
    public boolean willSoulSpawn;
    private void tickSoulCheck() {
        if (user != null && user.isAlive()) {
            boolean soulCanSpawn = 
                    usesResolve() && 
                    getResolveLevel() > 0 &&
                    JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).soulAscension.get() &&
                    !(JojoModUtil.isUndead(user) || OptionalDependencyHelper.vampirism().isEntityVampire(user)) &&
                    !(user instanceof PlayerEntity && user.level.getGameRules().getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN));
            if (this.willSoulSpawn != soulCanSpawn) {
                this.willSoulSpawn = soulCanSpawn;
                serverPlayerUser.ifPresent(player -> PacketManager.sendToClient(SoulSpawnPacket.spawnFlag(soulCanSpawn), player));
            }
        }
    }
    
    @Override
    public boolean willSoulSpawn() {
        return willSoulSpawn;
    }
    
    @Override
    public void clSetSoulSpawnFlag(boolean flag) {
        this.willSoulSpawn = flag;
    }
    
    @Override
    public boolean spawnSoulOnDeath() {
        tickSoulCheck();
        if (willSoulSpawn) {
            float resolveRatio = user.hasEffect(ModStatusEffects.RESOLVE.get()) ? 1 : getResolveRatio();
            int ticks = (int) (60 * (getResolveLevel() + resolveRatio));
            if (user.level.getLevelData().isHardcore()) {
                ticks += ticks / 2;
            }
            int ticksClsr = ticks;

            boolean resolveCanLvlUp = user.level.getLevelData().isHardcore()
                    || !JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).keepStandOnDeath.get();

            EntityUtilCap.queueOnTimeResume(user, () -> {
                if (user instanceof ServerPlayerEntity) {
                    ModCriteriaTriggers.SOUL_ASCENSION.get().trigger((ServerPlayerEntity) user, this, ticksClsr);
                }
                SoulEntity soulEntity = new SoulEntity(user.level, user, ticksClsr, resolveCanLvlUp);
                LivingEntity killer = user.getKillCredit();
                if (killer != null) {
                    soulEntity.setNoResolveToEntity(StandUtil.getStandUser(killer));
                }
                user.level.addFreshEntity(soulEntity);
            });
            return true;
        }
        
        serverPlayerUser.ifPresent(player -> PacketManager.sendToClient(SoulSpawnPacket.noSoulSpawned(), player));
        return false;
    }
    
    
    @Override
    public StandEffectsTracker getContinuousEffects() {
        return continuousEffects;
    }
    

    @Override
    public void skipProgression() {
        StandType<?> standType = getType();
        if (standType != null) {
            setProgressionSkipped();
            resolveCounter.setResolveLevel(getMaxResolveLevel());
            if (!user.level.isClientSide()) {
                standType.getAllUnlockableActions()
                .forEach(action -> {
                    actionLearningProgressMap.addEntry(action, getType());
                    setLearningProgressPoints(action, action.getMaxTrainingPoints(this));
                });
            }
        }
    }
    
    @Override
    public void setProgressionSkipped() {
        this.skippedProgression = true;
        clUpdateHud();
    }
    
    @Override
    public boolean wasProgressionSkipped() {
        return skippedProgression;
    }
    
    @Override
    public float getStatsDevelopment() {
        return usesResolve() ? (float) getResolveLevel() / (float) getMaxResolveLevel() : 0;
    }
    
    private boolean playerSkipsActionTraining() {
        return user != null && (user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild
                || JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).skipStandProgression.get());
    }
    
    @Override
    public boolean unlockAction(StandAction action) {
        if (actionLearningProgressMap.addEntry(action, getType())) {
            boolean getFull = !action.isTrained() || playerSkipsActionTraining();
            setLearningProgressPoints(action, getFull ? action.getMaxTrainingPoints(this) : 0F);
            return true;
        }
        return false;
    }
    
    @Override
    public float getLearningProgressRatio(Action<IStandPower> action) {
        if (action.isTrained() && action instanceof StandAction) {
            StandAction standAction = (StandAction) action;
            return getLearningProgressPoints(standAction) / standAction.getMaxTrainingPoints(this);
        }
        return super.getLearningProgressRatio(action);
    }
    
    public float getLearningProgressPoints(StandAction action) {
        return Math.min(actionLearningProgressMap.getLearningProgressPoints(action, getType()), action.getMaxTrainingPoints(this));
    }
    
    @Override
    public void setLearningProgressPoints(StandAction action, float points) {
        StandActionLearningEntry learningEntry = actionLearningProgressMap.setLearningProgressPoints(action, points, this);
        if (learningEntry != null && this.getUser() != null && !this.getUser().level.isClientSide()) {
            action.onTrainingPoints(this, getLearningProgressPoints(action));
            if (getLearningProgressPoints(action) == action.getMaxTrainingPoints(this)) {
                action.onMaxTraining(this);
            }
            
            serverPlayerUser.ifPresent(player -> {
                actionLearningProgressMap.syncEntryWithUser(learningEntry, player);
            });
        }
    }
    
    @Override
    public void setLearningFromPacket(StandActionLearningPacket packet) {
        actionLearningProgressMap.setEntryDirectly(packet.entry);
        clUpdateHud();
    }
    
    @Override
    public void addLearningProgressPoints(StandAction action, float points) {
        if (user != null && points > 0 && user.hasEffect(ModStatusEffects.RESOLVE.get())) {
            points *= 4;
        }
        
        float currentValue = actionLearningProgressMap.getLearningProgressPoints(action, getType());
        points = Math.max(currentValue + points, 0);
        float maxValue = action.getMaxTrainingPoints(this);
        if (currentValue <= maxValue) {
            points = Math.min(points, maxValue);
        }
        
        setLearningProgressPoints(action, points);
    }
    
    @Override
    public void fullStandClear() {
        this.actionLearningProgressMap = new StandActionLearningProgress();
        resolveCounter.clearLevels();
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new StandFullClearPacket(), player);
        });
        if (!hasPower()) {
            hadStand = false;
        }
        previousStands.clear();
        standArrowHandler.clear();
    }
    
    @Override
    public Iterable<StandAction> getAllUnlockedActions() {
        return actionLearningProgressMap.getAllUnlocked(this);
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
        else {
            serverPlayerUser.ifPresent(player -> {
                ITextComponent message;
                if (invalidReadStandId.isPresent()) {
                    message = invalidReadStandId.map(id -> id.getNamespace().equals(JojoMod.MOD_ID) ? 
                            new TranslationTextComponent("jojo.chat.message.no_stand.mod_version", id)
                            : new TranslationTextComponent("jojo.chat.message.no_stand.addon", id)).get();
                }
                else {
                    message = new TranslationTextComponent("jojo.chat.message.no_stand");
                }
                player.displayClientMessage(message, true);
            });
        }
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
            return !(standManifestation instanceof StandEntity && ((StandEntity) standManifestation).getCurrentTask().isPresent())
                    && getType().canLeap();
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
        if (standManifestation instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) standManifestation;
            float volume = standEntity.getLeapStrength() / 2.4F;
            ServerPlayerEntity except = serverPlayerUser.map(player -> {
                PacketManager.sendToClient(new PlaySoundAtStandEntityPacket(ModSounds.STAND_LEAP.get(), standEntity.getId(), 
                        volume, 1.0F), player);
                return player;
            }).orElse(null);
            standEntity.playSound(ModSounds.STAND_LEAP.get(), volume, 1.0F, except);
        }
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
        GeneralUtil.ifPresentOrElse(standInstance, 
                stand -> cnbt.put("StandInstance", stand.writeNBT()), 
                ()    -> invalidReadStandNbt.ifPresent(standNbt -> cnbt.put("StandInstance", standNbt)));
        
        cnbt.putBoolean("HadStand", hadStand);
        if (usesStamina()) {
            cnbt.putFloat("Stamina", stamina);
        }
        cnbt.put("Resolve", resolveCounter.writeNBT());
        cnbt.putBoolean("Skipped", skippedProgression);
        cnbt.put("ActionLearning", actionLearningProgressMap.toNBT());
        cnbt.put("Effects", continuousEffects.toNBT());
        cnbt.put("PrevStands", previousStands.toNBT());
        cnbt.put("ArrowHandler", standArrowHandler.toNBT());
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        StandInstance standInstance;
        if (nbt.contains("StandInstance", MCUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT standInstanceNbt = nbt.getCompound("StandInstance");
            standInstance = StandInstance.fromNBT(standInstanceNbt);
            
            if (standInstance == null) {
                invalidReadStandNbt = Optional.of(standInstanceNbt.copy());
                if (standInstanceNbt.contains("StandType", MCUtil.getNbtId(StringNBT.class))) {
                    invalidReadStandId = Optional.of(new ResourceLocation(standInstanceNbt.getString("StandType")));
                }
            }
        }
        else {
            standInstance = LegacyUtil.readOldStandCapType(nbt).orElse(null);
        }
        setStandInstance(standInstance);
            
        hadStand = nbt.getBoolean("HadStand");
        if (usesStamina()) {
            stamina = nbt.getFloat("Stamina");
        }
        resolveCounter.readNbt(nbt.getCompound("Resolve"));
        skippedProgression = nbt.getBoolean("Skipped");
        if (nbt.contains("ActionLearning", MCUtil.getNbtId(CompoundNBT.class))) {
            actionLearningProgressMap.fromNBT(nbt.getCompound("ActionLearning"));
        }
        if (nbt.contains("Effects", MCUtil.getNbtId(CompoundNBT.class))) {
            continuousEffects.fromNBT(nbt.getCompound("Effects"));
        }
        if (nbt.contains("PrevStands", MCUtil.getNbtId(CompoundNBT.class))) {
            previousStands.fromNBT(nbt.getCompound("PrevStands"));
        }
        if (nbt.contains("ArrowHandler", MCUtil.getNbtId(CompoundNBT.class))) {
            standArrowHandler.fromNBT(nbt.getCompound("ArrowHandler"));
        }
        super.readNBT(nbt);
    }

    @Override
    public void onClone(IStandPower oldPower, boolean wasDeath) {
        super.onClone(oldPower, wasDeath);
        StandPower oldImpl = (StandPower) oldPower;
        this.standArrowHandler.keepOnDeath(oldPower.getStandArrowHandler());
        this.actionLearningProgressMap = oldImpl.actionLearningProgressMap;
        this.invalidReadStandId = oldImpl.invalidReadStandId;
        this.invalidReadStandNbt = oldImpl.invalidReadStandNbt;
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
        this.continuousEffects = oldPower.getContinuousEffects();
        this.continuousEffects.setPowerData(this);
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
            actionLearningProgressMap.syncFullWithUser(player);
            if (skippedProgression) {
                PacketManager.sendToClient(new SkippedStandProgressionPacket(), player);
            }
            continuousEffects.syncWithUserOnly(player);
            previousStands.syncWithUser(player);
            standArrowHandler.syncWithUser(player);
            
            tickSoulCheck();
            PacketManager.sendToClient(SoulSpawnPacket.spawnFlag(willSoulSpawn), player);
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
