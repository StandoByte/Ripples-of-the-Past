package com.github.standobyte.jojo.power.stand;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolveLimitPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolvePacket;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
    public static final float RESOLVE_DMG_REDUCTION = 0.5F;
    private static final float MAX_RESOLVE = 1000;
    private static final float RESOLVE_DECAY = 5F;
    private static final int RESOLVE_NO_DECAY_TICKS = 400;
    private static final float RESOLVE_EFFECT_DMG_REDUCTION = 0.5F;
    private static final float RESOLVE_FOR_DMG_POINT = 0.5F;
    private static final float RESOLVE_LIMIT_FOR_DMG_POINT_TAKEN = 5F;
    private static final float RESOLVE_LIMIT_MANUAL_CONTROL_TICK = 0.1F;
    private static final float RESOLVE_UNDER_LIMIT_MULTIPLIER = 10F;
    private static final float RESOLVE_LIMIT_DECAY = 4F;
    private static final int RESOLVE_LIMIT_NO_DECAY_TICKS = 40;
    private static final int[] RESOLVE_EFFECT_MIN = {300, 400, 500, 600, 600};
    private static final int[] RESOLVE_EFFECT_MAX = {600, 1200, 1500, 1800, 2400};
    
    private float resolve = 0;
    private int resolveLevel = 0;
    private int noResolveDecayTicks = 0;
    private float maxAchievedResolve = 0;


    // TODO
    void reset() {
        resolve = 0;
        resolveLevel = 0;
        noResolveDecayTicks = 0;
        maxAchievedResolve = 0;
    }

    void onStandAcquired() {
    }

    // TODO
    void tick() {
    }
    
    
    
    float getResolveValue() {
        return resolve;
    }

    // TODO
    float getMaxResolveValue() {
        return MAX_RESOLVE;
    }
    
    float getMaxAchievedValue() {
        return maxAchievedResolve;
    }
    
    int getResolveLevel() {
        return resolveLevel;
    }
    


    // TODO
    public void setResolveValue(float resolve) {
        
    }

    // TODO
    public void addResolveValue(float resolve, IStandPower standPower) {
        
    }

    // TODO
    public void setResolveLevel(int level) {
        
    }

    // TODO
    void syncWithUser(ServerPlayerEntity user) {
//        PacketManager.sendToClient(new SyncResolvePacket(resolve, maxAchievedResolve, resolveLevel, noResolveDecayTicks), user);
//        PacketManager.sendToClient(new SyncResolveLimitPacket(resolveLimit, noResolveLimitDecayTicks), user);
    }
    
    

    // TODO
    public void onAttack(LivingEntity target, IStandDamageSource dmgSource, float dmgAmount) {
        /* add resolve (with boosts):
         * 
         */
    }

    // TODO
    public void onGettingAttacked(DamageSource dmgSource, float dmgAmount, LivingEntity user) {
        
    }

    // TODO
    public void onResolveEffectStarted(int amplifier, IStandPower stand) {
//        if (stand.getResolveLevel() < stand.getMaxResolveLevel()) {
//            stand.setResolveLevel(Math.min(amplifier + 1, stand.getMaxResolveLevel()));
//        }
//        stand.setResolve(stand.getMaxResolve(), 0);
    }

    // TODO
    public void onResolveEffectEnded(int amplifier, IStandPower stand) {
        
    }
    
    

    // TODO
    void readNbt(CompoundNBT nbt) {
//        resolve = nbt.getFloat("Resolve");
//        resolveLevel = nbt.getByte("ResolveLevel");
//        noResolveDecayTicks = nbt.getInt("ResolveTicks");
//        resolveLimit = nbt.getFloat("ResolveLimit");
//        maxAchievedResolve = nbt.getFloat("ResolveAchieved");
//        noResolveLimitDecayTicks = nbt.getInt("ResolveLimitTicks");
    }

    // TODO
    CompoundNBT writeNBT() {
        CompoundNBT resolveNbt = new CompoundNBT();
//        resolveNbt.putFloat("Resolve", resolve);
//        resolveNbt.putByte("ResolveLevel", (byte) resolveLevel);
//        resolveNbt.putInt("ResolveTicks", noResolveDecayTicks);
//        resolveNbt.putFloat("ResolveLimit", resolveLimit);
//        resolveNbt.putFloat("ResolveAchieved", maxAchievedResolve);
//        resolveNbt.putInt("ResolveLimitTicks", noResolveLimitDecayTicks);
        return resolveNbt;
    }

    // TODO
    void alwaysResetOnDeath() {
//        this.resolve = 0;
//        this.noResolveDecayTicks = 0;
//        this.resolveLimit = 0;
//        this.noResolveLimitDecayTicks = 0;
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
//    public void addResolve(float amount) {
//        if (usesResolve()) {
//            setResolve(getResolve() + amount, RESOLVE_NO_DECAY_TICKS);
//        }
//    }
//    
//    @Override
//    public void setResolve(float amount, int noDecayTicks) {
//        setResolve(amount, noDecayTicks, this.maxAchievedResolve);
//    }
//
//    @Override
//    public void setResolve(float amount, int noDecayTicks, float maxAchievedResolve) {
//        amount = MathHelper.clamp(amount, 0, getMaxResolve());
//        boolean send = this.resolve != amount || this.noResolveDecayTicks != noDecayTicks;
//        this.resolve = amount;
//        this.maxAchievedResolve = Math.max(maxAchievedResolve, this.resolve);
//        this.noResolveDecayTicks = Math.max(this.noResolveDecayTicks, noDecayTicks);
//        
//        if (!user.hasEffect(ModEffects.RESOLVE.get()) && this.resolve == getMaxResolve()) {
//            user.addEffect(new EffectInstance(ModEffects.RESOLVE.get(), 
//                    RESOLVE_EFFECT_MAX[Math.min(resolveLevel, RESOLVE_EFFECT_MAX.length)], resolveLevel, false, 
//                    false, true));
//            setResolveLevel(Math.min(resolveLevel + 1, getMaxResolveLevel()));
//        }
//        
//        if (send) {
//            serverPlayerUser.ifPresent(player -> {
//                PacketManager.sendToClient(new SyncResolvePacket(getResolve(), maxAchievedResolve, resolveLevel, noResolveDecayTicks), player);
//            });
//        }
//        
//        setResolveLimit(Math.max(resolveLimit, getResolve()), noDecayTicks);
//    }
//    
//    @Override
//    public int getNoResolveDecayTicks() {
//        return noResolveDecayTicks;
//    }
//    
//    @Override
//    public int getResolveLevel() {
//        return usesResolve() ? resolveLevel : 0;
//    }
//    
//    @Override
//    public void setResolveLevel(int level) {
//        if (usesResolve()) {
//            this.resolveLevel = level;
//            if (!user.level.isClientSide() && hasPower()) {
//                getType().onNewResolveLevel(this);
//                if (level >= getType().getMaxResolveLevel()) {
//                    serverPlayerUser.ifPresent(player -> {
//                        ModCriteriaTriggers.STAND_MAX.get().trigger(player);
//                    });
//                }
//            }
//        }
//    }
//    
//    @Override
//    public void resetResolve() {
//        this.maxAchievedResolve = 0;
//        setResolve(0, 0);
//        setResolveLimit(0, 0);
//    }
//    
//    @Override
//    public float getResolveLimit() {
//        return MathHelper.clamp(Math.max(resolveLimit, maxAchievedResolve), getResolve(), getMaxResolve());
//    }
//    
//    @Override
//    public void addResolveLimit(float amount) {
//        float hpRatio = user.getHealth() / user.getMaxHealth();
//        setResolveLimit(getResolveLimit() + amount, (int) ((float) RESOLVE_LIMIT_NO_DECAY_TICKS * (10F - hpRatio * 9F)));
//    }
//    
//    @Override
//    public void setResolveLimit(float amount, int noDecayTicks) {
//        amount = MathHelper.clamp(amount, 0, getMaxResolve());
//        boolean send = this.resolveLimit != amount;
//        this.resolveLimit = amount;
//        this.noResolveLimitDecayTicks = Math.max(this.noResolveLimitDecayTicks, noDecayTicks);
//        if (send) {
//            serverPlayerUser.ifPresent(player -> {
//                PacketManager.sendToClient(new SyncResolveLimitPacket(resolveLimit, noResolveLimitDecayTicks), player);
//            });
//        }
//    }
//    
//    @Override
//    public float getResolveDmgReduction() {
//        if (user.hasEffect(ModEffects.RESOLVE.get())) {
//            return RESOLVE_DMG_REDUCTION;
//        }
//        if (usesResolve()) {
//            return getResolveRatio() * RESOLVE_DMG_REDUCTION;
//        }
//        return 0;
//    }
//    
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
