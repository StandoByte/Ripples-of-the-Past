package com.github.standobyte.jojo.power.stand;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ResetResolveValuePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncMaxAchievedResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolveLevelPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolvePacket;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ResolveCounter {
    public static final float SOUL_TEAMMATE_RESOLVE_TICK = 0F;
    public static final float SOUL_LOOK_RESOLVE_TICK = 0F;
    public static final float RESOLVE_DMG_REDUCTION = 0.6667F;
    public static final Double[] DEFAULT_MAX_RESOLVE_VALUES = {100.0, 250.0, 500.0, 1000.0, 2500.0};
    private static final float RESOLVE_DECAY = 1F;
    private static final int RESOLVE_NO_DECAY_TICKS = 100;
    private static final float RESOLVE_FOR_DMG_POINT = 0.5F;
    private static final int RESOLVE_BOOST_NO_DECAY_TICKS = 100;
    private static final int RESOLVE_BOOST_MAX = 10;
    private static final float RESOLVE_BOOST_MIN_HP = 5;
    private static final float RESOLVE_BOOST_MAX_HP = 15;
    private static final int[] RESOLVE_EFFECT_MIN = {300, 400, 500, 600, 600};
    private static final int[] RESOLVE_EFFECT_MAX = {600, 1200, 1500, 1800, 2400};
    
    private final IStandPower stand;
    private final Optional<ServerPlayerEntity> serverPlayerUser;
    
    private float resolve = 0;
    private int noResolveDecayTicks = 0;
    
    private int resolveLevel = 0;
    
    private float gettingAttackedBoost = 0;
    private int boostDecayTicks = 0;
    
    private float maxAchievedResolve = 0;
    
    
    protected ResolveCounter(IStandPower stand, Optional<ServerPlayerEntity> serverPlayerUser) {
        this.stand = stand;
        this.serverPlayerUser = serverPlayerUser;
    }


    // TODO
    void reset() {
        resolve = 0;
        resolveLevel = 0;
        noResolveDecayTicks = 0;
        maxAchievedResolve = 0;
    }

    void onStandAcquired() {
    }

    void tick() {
        if (stand.getUser() != null && stand.getUser().hasEffect(ModEffects.RESOLVE.get())) {
            EffectInstance effect = stand.getUser().getEffect(ModEffects.RESOLVE.get());
            if (effect.getAmplifier() < RESOLVE_EFFECT_MIN.length) {
                resolve = Math.max(resolve - getMaxResolveValue() / (float) RESOLVE_EFFECT_MIN[effect.getAmplifier()], 0);
                if (!stand.getUser().level.isClientSide() && resolve == 0) {
                    stand.getUser().removeEffect(ModEffects.RESOLVE.get());
                }
            }
        }
        else {
            if (noResolveDecayTicks > 0) {
                noResolveDecayTicks--;
            }
            else {
                resolve = Math.max(resolve - getResolveDecay(), 0);
            }
        }
    }
    
    private float getResolveDecay() {
        return RESOLVE_DECAY;
    }
    
    
    
    float getResolveValue() {
        return resolve;
    }

    float getMaxResolveValue() {
        List<? extends Double> ptsList = JojoModConfig.getCommonConfigInstance(stand.getUser().level.isClientSide()).resolvePoints.get();
        return ptsList.get(Math.min(getResolveLevel(), ptsList.size() - 1)).floatValue();
    }
    
    public float getMaxAchievedValue() {
        return maxAchievedResolve;
    }
    
    int getResolveLevel() {
        return resolveLevel;
    }
    


    public void setResolveValue(float resolve, int noDecayTicks) {
        resolve = MathHelper.clamp(resolve, 0, getMaxResolveValue());
        if (noDecayTicks < 0) {
            noDecayTicks = this.noResolveDecayTicks;
        }
        
        boolean send = this.resolve != resolve || this.noResolveDecayTicks != noDecayTicks;
        this.maxAchievedResolve = Math.max(Math.max(this.resolve, resolve), maxAchievedResolve);
        this.resolve = resolve;
        this.noResolveDecayTicks = noDecayTicks;
        
        int ticks = noDecayTicks;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncResolvePacket(getResolveValue(), ticks), player);
            });
        }
        
        if (resolve == getMaxResolveValue() && stand.getUser() != null && !stand.getUser().level.isClientSide() && !stand.getUser().hasEffect(ModEffects.RESOLVE.get())) {
            stand.getUser().addEffect(new EffectInstance(ModEffects.RESOLVE.get(), 
                    RESOLVE_EFFECT_MAX[Math.min(resolveLevel, RESOLVE_EFFECT_MAX.length)], resolveLevel, false, 
                    false, true));
        }
    }
    
    public void setMaxAchievedValue(float value) {
        this.maxAchievedResolve = value;
    }

    public void addResolveValue(float resolve) {
        setResolveValue(getResolveValue() + boostAddedValue(resolve), RESOLVE_NO_DECAY_TICKS);
    }
    
    // TODO
    private float boostAddedValue(float value) {
        return value;
    }

    // TODO
    public float getPointsBoost(LivingEntity user) {
        if (user.getHealth() <= 5F) {
            return RESOLVE_BOOST_MAX;
        }
        return gettingAttackedBoost;
    }
    
    public void resetResolveValue() {
        this.resolve = 0;
        this.noResolveDecayTicks = 0;
        this.maxAchievedResolve = 0;
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new ResetResolveValuePacket(), player);
        });
    }

    void setResolveLevel(int level) {
        boolean send = this.resolveLevel != level;
        this.resolveLevel = level;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncResolveLevelPacket(getResolveLevel()), player);
            });
        }
    }
    
    

    public void onAttack(LivingEntity target, IStandDamageSource dmgSource, float dmgAmount) {
        if (stand.usesResolve() && target.getClassification(false) == EntityClassification.MONSTER || target.getType() == EntityType.PLAYER) {
            dmgAmount = Math.min(dmgAmount, target.getHealth());
            double attackStrength = Optional.ofNullable(target.getAttribute(Attributes.ATTACK_DAMAGE))
                    .map(ModifiableAttributeInstance::getValue).orElse(0.0);
            
            addResolveValue(dmgAmount * RESOLVE_FOR_DMG_POINT);
            if (stand.getUser().hasEffect(ModEffects.RESOLVE.get())) {
                setResolveValue(Math.max(getMaxResolveValue() * 0.5F, getResolveValue()), 0);
            }
        }
    }

    // TODO
    public void onGettingAttacked(DamageSource dmgSource, float dmgAmount, LivingEntity user) {
        Entity attacker = dmgSource.getEntity();
        if (stand.usesResolve() && attacker != null && !attacker.is(user)) {
            World world = attacker.level;
            if (!world.isClientSide()) {
                boolean noNaturalRegen = ((ServerWorld) world).getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
                float missingHpRatio = 1F - user.getHealth() / user.getMaxHealth();
                
            }
        }
    }

    public void onResolveEffectStarted(int amplifier, IStandPower stand) {
        stand.setResolveLevel(Math.min(amplifier + 1, stand.getMaxResolveLevel()));
        setResolveValue(stand.getMaxResolve(), 0);
    }

    public void onResolveEffectEnded(int amplifier, IStandPower stand) {
        resetResolveValue();
    }
    
    
    
    // TODO
    void clone(ResolveCounter previous) {
        this.resolve = previous.resolve;
        this.resolveLevel = previous.resolveLevel;
        this.noResolveDecayTicks = previous.noResolveDecayTicks;
        this.maxAchievedResolve = previous.maxAchievedResolve;
    }

    // TODO
    void alwaysResetOnDeath() {
        resolve = 0;
        noResolveDecayTicks = 0;
        maxAchievedResolve = 0;
    }

    // TODO
    void syncWithUser(ServerPlayerEntity player) {
        PacketManager.sendToClient(new SyncResolvePacket(getResolveValue(), noResolveDecayTicks), player);
        PacketManager.sendToClient(new SyncMaxAchievedResolvePacket(maxAchievedResolve), player);
        PacketManager.sendToClient(new SyncResolveLevelPacket(getResolveLevel()), player);
    }

    // TODO
    void readNbt(CompoundNBT nbt) {
        resolve = nbt.getFloat("Resolve");
        resolveLevel = nbt.getByte("ResolveLevel");
        noResolveDecayTicks = nbt.getInt("ResolveTicks");
        maxAchievedResolve = nbt.getFloat("ResolveAchieved");
    }

    // TODO
    CompoundNBT writeNBT() {
        CompoundNBT resolveNbt = new CompoundNBT();
        resolveNbt.putFloat("Resolve", resolve);
        resolveNbt.putByte("ResolveLevel", (byte) resolveLevel);
        resolveNbt.putInt("ResolveTicks", noResolveDecayTicks);
        resolveNbt.putFloat("ResolveAchieved", maxAchievedResolve);
        return resolveNbt;
    }
 
    
    
    
    
    
    
    

//  void addResolve(float amount);
//  void setResolve(float amount, int noDecayTicks);
//  void setResolve(float amount, int noDecayTicks, float maxAchievedResolve);
//  int getNoResolveDecayTicks();
//    void setResolveLevel(int level);
//    float getResolveLimit();
//    void addResolveLimit(float amount);
//    void setResolveLimit(float amount, int noDecayTicks);
//    void resetResolve();
    
    
    
    
    
    
    
    
    
    
    
    
    

    
//    @Override
//    public void addResolveOnAttack(LivingEntity target, float damageAmount) {
//        if (usesResolve() && target.getClassification(false) == EntityClassification.MONSTER || target.getType() == EntityType.PLAYER) {
//            damageAmount = Math.min(damageAmount, target.getHealth());
//            float resolveBase = damageAmount * RESOLVE_FOR_DMG_POINT;
//            float resolveHasMultiplier = MathHelper.clamp(getResolveLimit() - getResolve(), 0, resolveBase);
//            float resolveNoMultiplier = Math.max(resolveBase - resolveHasMultiplier, 0);
//            addResolve(resolveHasMultiplier * RESOLVE_UNDER_LIMIT_MULTIPLIER + resolveNoMultiplier);
//        }
//    }
//    
//    @Override
//    public void addResolveOnTakingDamage(DamageSource damageSource, float damageAmount) {
//        if (usesResolve() && damageSource.getEntity() != null) {
//            World world = damageSource.getEntity().level;
//            if (!world.isClientSide()) {
//                boolean noNaturalRegen = ((ServerWorld) world).getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
//                addResolveLimit(damageAmount * RESOLVE_LIMIT_FOR_DMG_POINT_TAKEN * (noNaturalRegen ? 1F : 2F));
//                setResolve(getResolve(), RESOLVE_NO_DECAY_TICKS);
//            }
//        }
//    }
//    
//    private void tickResolve() {
//        if (usesResolve()) {
//            resolveCounter.tick();
//        }
//        float decay = 0;
//        EffectInstance resolveEffect = user.getEffect(ModEffects.RESOLVE.get());
//        if (resolveEffect != null) {
//            if (resolveEffect.getAmplifier() < RESOLVE_EFFECT_MIN.length) {
//                decay = getMaxResolve() / (float) RESOLVE_EFFECT_MIN[resolveEffect.getAmplifier()];
//                if (!user.level.isClientSide() && decay >= resolve) {
//                    user.removeEffect(ModEffects.RESOLVE.get());
//                }
//            }
//            
//            resolveLimit = getMaxResolve();
//        }
//        else {
//            boolean noDecay = noResolveDecayTicks > 0;
//            if (noDecay) {
//                noResolveDecayTicks--;
//            }
//            if (!noDecay) {
//                decay = RESOLVE_DECAY;
//            }
//            if (isActive()) {
//                if (getStandManifestation() instanceof StandEntity && ((StandEntity) getStandManifestation()).isManuallyControlled()) {
//                    resolveLimit = Math.min(resolveLimit + RESOLVE_LIMIT_MANUAL_CONTROL_TICK, getMaxResolve());
//                    noResolveLimitDecayTicks = 1;
//                }
//                else {
//                    decay /= 4F;
//                }
//            }
//            
//            if (noResolveLimitDecayTicks > 0) {
//                noResolveLimitDecayTicks--;
//            }
//            else {
//                float hpRatio = user.getHealth() / user.getMaxHealth();
//                resolveLimit = Math.max(resolveLimit - RESOLVE_LIMIT_DECAY * hpRatio * hpRatio, 0);
//            }
//        }
//        
//        resolve = Math.max(resolve - decay, 0);
//    }
    
}
