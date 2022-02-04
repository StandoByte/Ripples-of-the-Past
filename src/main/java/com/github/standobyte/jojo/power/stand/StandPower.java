package com.github.standobyte.jojo.power.stand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.BalanceTestServerConfig;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolveLimitPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStaminaPacket;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.PowerBaseImpl;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class StandPower extends PowerBaseImpl<IStandPower, StandType<?>> implements IStandPower {
    private int tier = 0;
    @Nullable
    private IStandManifestation standManifestation = null;
    private float stamina;
    
    private float resolve;
    private int noResolveDecayTicks;
    private int resolveLevel;
    private float resolveLimit;
    private int noResolveLimitDecayTicks;
    @Deprecated
    private int xp = 0;
    private boolean skippedProgression;
    
    private Map<Action<IStandPower>, Float> actionLearningProgressMap = new HashMap<>();
    
    
    public StandPower(LivingEntity user) {
        super(user);
    }

    @Override
    public boolean givePower(StandType<?> standType) {
        if (super.givePower(standType)) {
            serverPlayerUser.ifPresent(player -> {
                SaveFileUtilCapProvider.getSaveFileCap(player).addPlayerStand(standType);
            });
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
            resolve = 0;
            noResolveDecayTicks = 0;
            resolveLevel = 0;
            resolveLimit = 0;
            noResolveLimitDecayTicks = 0;
            xp = 0;
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
        if (JojoModConfig.COMMON.skipStandProgression.get()
                || user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild) {
            skipProgression();
        }
        if (usesStamina()) {
            stamina = isUserCreative() ? getMaxStamina() : 0;
        }
        if (usesResolve()) {
            resolve = 0;
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
        return JojoModConfig.COMMON.standStamina.get() ? stamina : getMaxStamina();
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
        if (isUserCreative()) {
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
                if (getStamina() < getMaxStamina()) {
                    player.causeFoodExhaustion(0.005F);
                }
                if (player.getFoodData().getFoodLevel() > 17) {
                    staminaRegen *= 1.5F;
                }
            }
            addStamina(staminaRegen, false);
        }
    }
    
    private float getStaminaDurabilityModifier() {
        if (hasPower()) {
            StandStats stats = getType().getStats();
            return StandStatFormulas.getStaminaMultiplier(stats.getBaseDurability() + stats.getDevDurability(getStatsDevelopment()));
        }
        return 1F;
    }
    

    private static final float RESOLVE_DECAY = 5F;
    private static final int RESOLVE_NO_DECAY_TICKS = 400;
    private static final float RESOLVE_DMG_REDUCTION = 0.5F;
    private static final float RESOLVE_EFFECT_DMG_REDUCTION = 0.8F;
    private static final float RESOLVE_FOR_DMG_POINT = 0.5F;
    private static final float RESOLVE_LIMIT_FOR_DMG_POINT_TAKEN = 15F;
    private static final float RESOLVE_LIMIT_MANUAL_CONTROL_TICK = 0.1F;
    private static final float RESOLVE_UNDER_LIMIT_MULTIPLIER = 20F;
    private static final float RESOLVE_LIMIT_DECAY = 4F;
    private static final int RESOLVE_LIMIT_NO_DECAY_TICKS = 40;
    @Override
    public boolean usesResolve() {
        return hasPower() && getType().usesResolve();
    }
    
    @Override
    public float getResolve() {
        if (!usesResolve()) {
            return 0;
        }
        return resolve;
    }

    @Override
    public float getMaxResolve() {
        if (!usesResolve()) {
            return 0;
        }
        return MAX_RESOLVE;
    }
    
    @Override
    public void addResolve(float amount) {
        if (usesResolve()) {
            setResolve(this.resolve + amount, RESOLVE_NO_DECAY_TICKS);
        }
    }

    @Override
    public void setResolve(float amount, int noDecayTicks) {
        amount = MathHelper.clamp(amount, 0, getMaxResolve());
        boolean send = this.resolve != amount || this.noResolveDecayTicks != noDecayTicks;
        this.resolve = amount;
        this.noResolveDecayTicks = Math.max(this.noResolveDecayTicks, noDecayTicks);
        
        if (!user.hasEffect(ModEffects.RESOLVE.get()) && this.resolve == getMaxResolve()) {
            setResolveLevel(Math.min(resolveLevel + 1, getMaxResolveLevel()));
            user.addEffect(new EffectInstance(ModEffects.RESOLVE.get(), 
                    BalanceTestServerConfig.TEST_CONFIG.resolveEffectCapDuration.get(), resolveLevel - 1, false, 
                    false, true));
        }
        
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncResolvePacket(getResolve(), resolveLevel, noResolveDecayTicks), player);
            });
        }
        
        setResolveLimit(Math.max(resolveLimit, getResolve()), noDecayTicks);
    }
    
    @Override
    public int getNoResolveDecayTicks() {
        return noResolveDecayTicks;
    }
    
    @Override
    public int getResolveLevel() {
        return usesResolve() ? resolveLevel : 0;
    }
    
    @Override
    public int getMaxResolveLevel() {
        if (!usesResolve() && !hasPower()) {
            return 0;
        }
        return getType().getResolveLevels();
    }
    
    @Override
    public void setResolveLevel(int level) {
        if (usesResolve()) {
            this.resolveLevel = level;
            if (!user.level.isClientSide() && hasPower()) {
                // FIXME unlock new actions
                getType().onNewResolveLevel(this);
            }
        }
    }
    
    @Override
    public float getResolveLimit() {
        return MathHelper.clamp(resolveLimit, getResolve(), getMaxResolve());
    }
    
    @Override
    public void addResolveLimit(float amount) {
        float hpRatio = user.getHealth() / user.getMaxHealth();
        setResolveLimit(getResolveLimit() + amount, (int) ((float) RESOLVE_LIMIT_NO_DECAY_TICKS * (10F - hpRatio * 9F)));
    }
    
    @Override
    public void setResolveLimit(float amount, int noDecayTicks) {
        amount = MathHelper.clamp(amount, 0, getMaxResolve());
        boolean send = this.resolveLimit != amount;
        this.resolveLimit = amount;
        this.noResolveLimitDecayTicks = Math.max(this.noResolveLimitDecayTicks, noDecayTicks);
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncResolveLimitPacket(resolveLimit, noResolveLimitDecayTicks), player);
            });
        }
    }
    
    @Override
    public float getResolveDmgReduction() {
        if (user.hasEffect(ModEffects.RESOLVE.get())) {
            return RESOLVE_EFFECT_DMG_REDUCTION;
        }
        if (usesResolve()) {
            return getResolveRatio() * RESOLVE_DMG_REDUCTION;
        }
        return 0;
    }
    
    @Override
    public void addResolveOnAttack(LivingEntity target, float damageAmount) {
        if (usesResolve() && target.getClassification(false) == EntityClassification.MONSTER || target.getType() == EntityType.PLAYER) {
            damageAmount = Math.min(damageAmount, target.getHealth());
            float resolveBase = damageAmount * RESOLVE_FOR_DMG_POINT;
            float resolveHasMultiplier = MathHelper.clamp(getResolveLimit() - getResolve(), 0, resolveBase);
            float resolveNoMultiplier = Math.max(resolveBase - resolveHasMultiplier, 0);
            addResolve(resolveHasMultiplier * RESOLVE_UNDER_LIMIT_MULTIPLIER + resolveNoMultiplier);
        }
    }
    
    @Override
    public void addResolveOnTakingDamage(DamageSource damageSource, float damageAmount) {
        if (usesResolve() && damageSource.getEntity() != null) {
            World world = damageSource.getEntity().level;
            if (!world.isClientSide()) {
                boolean noNaturalRegen = ((ServerWorld) world).getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
                addResolveLimit(damageAmount * RESOLVE_LIMIT_FOR_DMG_POINT_TAKEN * (noNaturalRegen ? 1F : 2F));
                setResolve(getResolve(), RESOLVE_NO_DECAY_TICKS);
            }
        }
    }
    
    private void tickResolve() {
        float decay = 0;
        EffectInstance resolveEffect = user.getEffect(ModEffects.RESOLVE.get());
        if (resolveEffect != null) {
            decay = getMaxResolve() / BalanceTestServerConfig.TEST_CONFIG.resolveEffectBaseDuration.get();
            decay *= (getMaxResolveLevel() - Math.min(resolveEffect.getAmplifier(), getMaxResolveLevel()));
            if (decay >= resolve) {
                user.removeEffect(ModEffects.RESOLVE.get());
            }
            
            resolveLimit = getMaxResolve();
        }
        else {
            boolean noDecay = noResolveDecayTicks > 0;
            if (noDecay) {
                noResolveDecayTicks--;
            }
            if (!noDecay) {
                decay = RESOLVE_DECAY;
            }
            if (isActive()) {
                if (getStandManifestation() instanceof StandEntity && ((StandEntity) getStandManifestation()).isManuallyControlled()) {
                    resolveLimit = Math.min(resolveLimit + RESOLVE_LIMIT_MANUAL_CONTROL_TICK, getMaxResolve());
                    noResolveLimitDecayTicks = 1;
                }
                else {
                    decay /= 4F;
                }
            }
            
            if (noResolveLimitDecayTicks > 0) {
                noResolveLimitDecayTicks--;
            }
            else {
                float hpRatio = user.getHealth() / user.getMaxHealth();
                resolveLimit = Math.max(resolveLimit - RESOLVE_LIMIT_DECAY * hpRatio * hpRatio, 0);
            }
        }
        
        resolve = Math.max(resolve - decay, 0);
    }
    
    
    
    @Override
    public void skipProgression() {
        this.skippedProgression = true;
        setResolveLevel(getMaxResolveLevel());
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
        // FIXME stand progression
        return false;
    }

    @Override
    public float getLearningProgress(Action<IStandPower> action) {
        return actionLearningProgressMap.getOrDefault(action, 0F);
    }

    @Override
    public void setLearningProgress(Action<IStandPower> action, float progress) {
        progress = MathHelper.clamp(progress, 0F, 1F);
        actionLearningProgressMap.put(action, progress);
        // FIXME sync learning progress to client
    }

    @Override
    public void addLearningProgress(Action<IStandPower> action, float progress) {
        setLearningProgress(action, getLearningProgress(action) + progress);
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
    public int getTier() {
        return tier;
    }
    
//    @Override // TODO Stand Sealing effect
//    public boolean canUsePower() {
//        return super.canUsePower() && !user.hasEffect(ModEffects.STAND_SEALING.get());
//    }
    
    @Override
    public boolean isLeapUnlocked() {
        if (standManifestation instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) standManifestation;
            return !standEntity.isArmsOnlyMode() && standEntity.isFollowingUser();
        }
        return false;
    }
    
    @Override
    public float leapStrength() {
        StandEntity standEntity = (StandEntity) standManifestation;
        if (standEntity.isFollowingUser()) {
            return StandStatFormulas.getLeapStrength(standEntity.getAttackDamage(null));
        }
        return 0;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 0;
    }
    
    @Override
    public void onLeap() {
        super.onLeap();
        consumeStamina(200);
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = super.writeNBT();
        cnbt.putString("StandType", ModStandTypes.Registry.getKeyAsString(getType()));
        if (usesStamina()) {
            cnbt.putFloat("Stamina", stamina);
        }
        if (usesResolve()) {
            cnbt.putFloat("Resolve", resolve);
            cnbt.putByte("ResolveLevel", (byte) resolveLevel);
            cnbt.putInt("ResolveTicks", noResolveDecayTicks);
            cnbt.putFloat("ResolveLimit", resolveLimit);
            cnbt.putInt("ResolveLimitTicks", noResolveLimitDecayTicks);
        }
        cnbt.putInt("Exp", getXp());
        cnbt.putBoolean("Skipped", skippedProgression);
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        String standName = nbt.getString("StandType");
        if (standName != IPowerType.NO_POWER_NAME) {
            StandType<?> stand = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standName));
            if (stand != null) {
                setType(stand);
                xp = nbt.getInt("Exp");
            }
        }
        if (usesStamina()) {
            stamina = nbt.getFloat("Stamina");
        }
        if (usesResolve()) {
            resolve = nbt.getFloat("Resolve");
            resolveLevel = nbt.getByte("ResolveLevel");
            noResolveDecayTicks = nbt.getInt("ResolveTicks");
            resolveLimit = nbt.getFloat("ResolveLimit");
            noResolveLimitDecayTicks = nbt.getInt("ResolveLimitTicks");
        }
        skippedProgression = nbt.getBoolean("Skipped");
        super.readNBT(nbt);
    }
    
    @Override
    protected void keepPower(IStandPower oldPower, boolean wasDeath) {
        super.keepPower(oldPower, wasDeath);
        this.xp = oldPower.getXp();
        this.stamina = oldPower.getStamina();
        if (!wasDeath) {
            this.resolve = oldPower.getResolve();
            this.noResolveDecayTicks = oldPower.getNoResolveDecayTicks();
            StandPower cast = (StandPower) oldPower;
            this.resolveLimit = cast.resolveLimit;
            this.noResolveLimitDecayTicks = cast.noResolveLimitDecayTicks;
        }
        this.resolveLevel = oldPower.getResolveLevel();
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
                    PacketManager.sendToClient(new SyncResolvePacket(resolve, resolveLevel, noResolveDecayTicks), player);
                    PacketManager.sendToClient(new SyncResolveLimitPacket(resolveLimit, noResolveLimitDecayTicks), player);
                }
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
