package com.github.standobyte.jojo.power.stand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.BalanceTestServerConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStaminaPacket;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.PowerBaseImpl;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class StandPower extends PowerBaseImpl<IStandPower, StandType<?>> implements IStandPower {
    private int tier = 0;
    @Nullable
    private IStandManifestation standManifestation = null;
    private float stamina;
    
    private float resolve;
    private float achievedResolve;
    private int noResolveDecayTicks;
    @Deprecated
    private int xp = 0;
    
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
    protected void onTypeInit(StandType<?> standType) {
        attacks = Arrays.asList(standType.getAttacks());
        abilities = Arrays.asList(standType.getAbilities());
        if (user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild) {
            xp = StandPower.MAX_EXP;
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
        return stamina;
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
        return maxAmount;
    }
    
    @Override
    public void addStamina(float amount) {
        setStamina(MathHelper.clamp(this.stamina + amount, 0, getMaxStamina()));
    }

    @Override
    public void consumeStamina(float amount) {
        if (!isUserCreative()) {
            setStamina(this.stamina - amount);
        }
    }

    @Override
    public void setStamina(float amount) {
        amount = MathHelper.clamp(amount, 0, getMaxStamina());
        boolean send = this.stamina != amount;
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
            staminaRegen *= serverPlayerUser.map(player -> {
                if (getStamina() < getMaxStamina()) {
                    player.causeFoodExhaustion(0.005F);
                }
                if (player.getFoodData().getFoodLevel() > 17) {
                    return 1.5F;
                }
                return 1F;
            }).orElse(1F);
            stamina = Math.min(stamina + staminaRegen, getMaxStamina());
        }
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
        return resolve;
    }

    @Override
    public float getAchievedResolve() {
        if (!usesResolve()) {
            return 0;
        }
        return achievedResolve;
    }

    @Override
    public float getMaxResolve() {
        if (!usesResolve()) {
            return 0;
        }
        return BalanceTestServerConfig.SERVER_CONFIG.maxResolve.get().floatValue();
    }
    
    @Override
    public int getNoResolveDecayTicks() {
        return noResolveDecayTicks;
    }
    
    @Override
    public void addResolve(float amount) {
        if (usesResolve()) {
            float modifierForAchieved = BalanceTestServerConfig.SERVER_CONFIG.resolveModifierAchieved.get().floatValue();
            float boostedAmount = Math.min(Math.max(achievedResolve - resolve, 0), amount * modifierForAchieved);
            amount += boostedAmount * (modifierForAchieved - 1) / modifierForAchieved;
            setResolve(MathHelper.clamp(this.resolve + amount, 0, getMaxResolve()), -999, BalanceTestServerConfig.SERVER_CONFIG.noResolveDecayTicks.get());
        }
    }

    @Override
    public void setResolve(float amount, float achievedResolve, int noDecayTicks) {
        amount = MathHelper.clamp(amount, 0, getMaxResolve());
        boolean send = this.resolve != amount || this.noResolveDecayTicks != noDecayTicks;
        this.resolve = amount;
        this.achievedResolve = Math.max(Math.max(this.resolve, achievedResolve), this.achievedResolve);
        this.noResolveDecayTicks = Math.max(this.noResolveDecayTicks, noDecayTicks);
        if (this.resolve == getMaxResolve()) {
            getUser().addEffect(new EffectInstance(ModEffects.RESOLVE.get(), 
                    Math.max(this.noResolveDecayTicks, BalanceTestServerConfig.SERVER_CONFIG.resolveModeTicks.get()), 0, false, 
                    false, true));
        }
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncResolvePacket(getResolve(), this.achievedResolve, this.noResolveDecayTicks), player);
            });
        }
    }
    
    private void tickResolve() {
        if (noResolveDecayTicks > 0) {
            noResolveDecayTicks--;
        }
        else if (!user.hasEffect(ModEffects.RESOLVE.get())){
            resolve = Math.max(resolve - BalanceTestServerConfig.SERVER_CONFIG.resolveDecay.get().floatValue(), 0);
        }
    }
    
    
    @Override
    public int getXp() {
        return MAX_EXP; // FIXME stand progression
    }

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
        // FIXME sync to client
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
        return standManifestation instanceof StandEntity && !((StandEntity) standManifestation).lowerStatsFromArmsOnly();
    }
    
    @Override
    public float leapStrength() {
        StandEntity standEntity = (StandEntity) standManifestation;
        if (standEntity.isFollowingUser()) {
            return (float) standEntity.getStats().getDamage() / 3F;
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
            cnbt.putFloat("AchievedResolve", achievedResolve);
            cnbt.putInt("ResolveTicks", noResolveDecayTicks);
        }
        cnbt.putInt("Exp", getXp());
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
            achievedResolve = nbt.getFloat("AchievedResolve");
            noResolveDecayTicks = nbt.getInt("ResolveTicks");
        }
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
        }
        this.achievedResolve = oldPower.getAchievedResolve();
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
                    PacketManager.sendToClient(new SyncResolvePacket(resolve, achievedResolve, noResolveDecayTicks), player);
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
