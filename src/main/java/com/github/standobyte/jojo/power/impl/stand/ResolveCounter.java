package com.github.standobyte.jojo.power.impl.stand;

import java.util.Optional;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.MaxAchievedResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetResolveValuePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveBoostsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveLevelPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolvePacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.DiscardingSortedMultisetWrapper;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.LegacyUtil;
import com.google.common.collect.BoundType;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

public class ResolveCounter {
    public static final float RESOLVE_DMG_REDUCTION = 0.6667F;
    public static final Double[] DEFAULT_MAX_RESOLVE_VALUES = {2500.0, 10000.0, 25000.0, 50000.0, 32500.0};
    private static final float RESOLVE_DECAY = 2F;
    private static final int RESOLVE_NO_DECAY_TICKS = 400;
    private static final float RESOLVE_FOR_DMG_POINT = 1F;
    private static final int[] RESOLVE_EFFECT_MIN = {300, 400, 500, 600, 600};
    private static final int[] RESOLVE_EFFECT_MAX = {600, 1200, 1500, 1800, 2400};

    
    private static final float BOOST_ATTACK_MAX = 5F;
    private static final float BOOST_PER_DMG_DEALT = 0.05F;
    
    private static final float BOOST_MISSING_HP_MAX = 10F;
    private static final float BOOST_MIN_HP = 5F;
    private static final float BOOST_MAX_HP = 15F;
    
    private static final float BOOST_REMOTE_MAX = 5F;
    private static final float BOOST_REMOTE_PER_TICK = 0.025F;
    
    private static final float BOOST_CHAT_MAX = 1.25F;
    private static final float BOOST_PER_CHARACTER = 0.05F;
    
    private final IStandPower stand;
    private final Optional<ServerPlayerEntity> serverPlayerUser;
    
    private float resolve = 0;
    private float prevTickResolve;
    private int noResolveDecayTicks = 0;
    
    private ResolveLevelsMap levels = new ResolveLevelsMap();
    
    private DiscardingSortedMultisetWrapper<Float> resolveRecords = 
            new DiscardingSortedMultisetWrapper<>(10);
    private boolean saveNextRecord = true;
    private float maxAchievedValue;
    
    private float boostAttack = 1;
    private float boostRemoteControl = 1;
    private float boostChat = 1;
    private float hpOnGettingAttacked = -1;
    
    
    protected ResolveCounter(IStandPower stand) {
        this.stand = stand;
        LivingEntity standUser = stand.getUser();
        this.serverPlayerUser = standUser instanceof ServerPlayerEntity ? Optional.of((ServerPlayerEntity) standUser) : Optional.empty();
    }


    void onStandAcquired(StandType<?> standType) {
        levels.onStandSet(standType);
    }

    void tick() {
        prevTickResolve = resolve;
        LivingEntity user = stand.getUser();
        if (user != null && user.hasEffect(ModEffects.RESOLVE.get())) {
            EffectInstance effect = user.getEffect(ModEffects.RESOLVE.get());
            if (effect.getAmplifier() < RESOLVE_EFFECT_MIN.length) {
                int effectLevel = effect.getAmplifier();
                if (effectLevel < 0) {
                    effectLevel = 255;
                }
                resolve = Math.max(resolve - getMaxResolveValue() / 
                        (float) RESOLVE_EFFECT_MIN[Math.min(effectLevel, RESOLVE_EFFECT_MIN.length)], 0);
                if (!user.level.isClientSide() && resolve == 0) {
                    user.removeEffect(ModEffects.RESOLVE.get());
                }
            }
        }
        else {
            if (noResolveDecayTicks > 0) {
                noResolveDecayTicks--;
                if (noResolveDecayTicks > 0) {
                    if (!stand.isActive()) {
                        noResolveDecayTicks--;
                    }
                }
                else {
                    if (saveNextRecord) {
                        saveNextRecord = false;
                    }
                    else {
                        resolveRecords.discardMin();
                    }
                    if (resolve > 0) {
                        resolveRecords.add(resolve);
                    }
                    setMaxAchievedValue(Optional.ofNullable(resolveRecords.getMax()).orElse(0F));
                }
            }
            else {
                boolean hadValue = resolve > 0;
                if (hadValue) {
                    boostAttack = 1;
                }
                resolve = Math.max(resolve - getResolveDecay(), 0);
                if (hadValue && resolve == 0) {
                    boostChat = 1;
                    hpOnGettingAttacked = -1;
                    saveNextRecord = true;
                }
                else if (user != null && user.getHealth() == user.getMaxHealth()) {
                    hpOnGettingAttacked = -1;
                }
            }
        }
        tickBoostRemoteControl();
    }
    
    private float getResolveDecay() {
        return RESOLVE_DECAY;
    }
    
    float getPrevTickResolveValue() {
        return prevTickResolve;
    }
    
    
    
    float getResolveValue() {
        return resolve;
    }

    float getMaxResolveValue() {
        return GeneralUtil.getOrLast(
                JojoModConfig.getCommonConfigInstance(stand.getUser().level.isClientSide()).resolveLvlPoints.get(), 
                getResolveLevel()).floatValue();
    }
    
    public float getMaxAchievedValue() {
        return maxAchievedValue;
    }
    
    public void setMaxAchievedValue(float value) {
        boolean send = this.maxAchievedValue != value;
        this.maxAchievedValue = value;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new MaxAchievedResolvePacket(value), player);
            });
        }
    }
    
    int getResolveLevel() {
        return levels.getResolveLevel(stand);
    }
    
    void clearLevels() {
        levels.clear();
    }
    


    public void setResolveValue(float resolve, int noDecayTicks) {
        resolve = MathHelper.clamp(resolve, 0, getMaxResolveValue());
        if (noDecayTicks < 0) {
            noDecayTicks = this.noResolveDecayTicks;
        }
        
        boolean send = this.resolve != resolve || this.noResolveDecayTicks != noDecayTicks;
        this.resolve = resolve;
        this.prevTickResolve = resolve;
        this.noResolveDecayTicks = noDecayTicks;
        
        int ticks = noDecayTicks;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new ResolvePacket(getResolveValue(), ticks), player);
            });
        }
        
        int resolveLevel = getResolveLevel();
        if (resolve == getMaxResolveValue() && stand.getUser() != null && !stand.getUser().level.isClientSide() && !stand.getUser().hasEffect(ModEffects.RESOLVE.get())) {
            stand.getUser().addEffect(new EffectInstance(ModEffects.RESOLVE.get(), 
                    RESOLVE_EFFECT_MAX[Math.min(resolveLevel, RESOLVE_EFFECT_MAX.length - 1)], resolveLevel, false, 
                    false, true));
        }
    }

    public void addResolveValue(float resolve, LivingEntity user) {
        setResolveValue(getResolveValue() + boostAddedValue(resolve, stand.getUser()), RESOLVE_NO_DECAY_TICKS);
        if (user.hasEffect(ModEffects.RESOLVE.get())) {
            setResolveValue(Math.max(getMaxResolveValue() * 0.5F, getResolveValue()), 0);
        }
    }
    
    
    
    private float boostAddedValue(float value, LivingEntity entity) {
        value *= boostFromAttack() * boostFromGettingAttacked(entity);
        value = multiplyRecords(value);
        return value;
    }
    
    private float multiplyRecords(float addedValue) {
        float totalBoostedValue = 0;
        SortedMultiset<Float> higherRecords = resolveRecords.getWrappedSet().tailMultiset(this.resolve, BoundType.OPEN);
        float currentResolve = getResolveValue();
        for (Multiset.Entry<Float> entry : higherRecords.entrySet()) {
            float upperBorder = entry.getElement();
            float multiplier = 1 + entry.getCount();
            totalBoostedValue += Math.min(addedValue, upperBorder - currentResolve) * multiplier;
        }
        return totalBoostedValue + addedValue;
    }
    
    private float boostFromAttack() {
        return boostAttack;
    }
    
    private float boostFromGettingAttacked(LivingEntity user) {
        if (INonStandPower.getNonStandPowerOptional(user).map(power -> power.getType() == ModPowers.VAMPIRISM.get()).orElse(false)) {
           return BOOST_MISSING_HP_MAX / 2;
        }
        float hp = user.getHealth();
        if (hpOnGettingAttacked > -1 && hpOnGettingAttacked < hp) {
            hp = hpOnGettingAttacked;
        }
        hp = MathHelper.clamp(hp, BOOST_MIN_HP, BOOST_MAX_HP);
        float boost = MathHelper.clamp((BOOST_MAX_HP - hp) * (BOOST_MISSING_HP_MAX - 1) / (BOOST_MAX_HP - BOOST_MIN_HP) + 1, 0, BOOST_MAX_HP);
        return boost;
    }
    
    public float getBoostVisible(LivingEntity user) {
        float boost = boostFromAttack() * boostFromGettingAttacked(user) * boostChat * boostRemoteControl;
//        if (getMaxAchievedValue() > getResolveValue()) {
//            boost *= BOOST_ALREADY_ACHIEVED;
//        }
        return boost;
    }

    void setResolveLevel(int level, boolean showUnlockToast) {
        boolean send = levels.setResolveLevel(stand, level);
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new ResolveLevelPacket(getResolveLevel(), showUnlockToast), player);
            });
        }
    }
    
    

    public void addResolveOnAttack(float dmgAmount) {
        if (stand.usesResolve()) {
            LivingEntity user = stand.getUser();
            float points = dmgAmount * RESOLVE_FOR_DMG_POINT;
            addResolveValue(points, user);
            if (!user.level.isClientSide() && boostAttack < BOOST_ATTACK_MAX) {
                float boost = dmgAmount * BOOST_PER_DMG_DEALT;
                boostAttack = Math.min(boostAttack + boost, BOOST_ATTACK_MAX);
                if (user instanceof ServerPlayerEntity) {
                    PacketManager.sendToClient(new ResolveBoostsPacket(boostAttack, boostRemoteControl, boostChat, hpOnGettingAttacked), (ServerPlayerEntity) user);
                }
            }
        }
    }

    public void onGettingAttacked(DamageSource dmgSource, float dmgAmount, LivingEntity user) {
        Entity attacker = dmgSource.getEntity();
        if (attacker != null && !attacker.level.isClientSide() && stand.usesResolve() && attacker != null && !attacker.is(user)) {
            float hp = Math.max(user.getHealth() - dmgAmount, 0);
            if (hpOnGettingAttacked > -1) {
                hp = Math.min(hp, hpOnGettingAttacked);
            }
            hpOnGettingAttacked = hp;
            
            if (user instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(new ResolveBoostsPacket(boostAttack, boostRemoteControl, boostChat, hpOnGettingAttacked), (ServerPlayerEntity) user);
            }
            
            if (dmgAmount >= user.getMaxHealth() * 0.4F) {
                addResolveValue(dmgAmount * BOOST_PER_DMG_DEALT * 2, user);
            }
        }
    }

    private void tickBoostRemoteControl() {
        if (stand.isActive() && stand.getUser() != null) {
            IStandManifestation standManifestation = stand.getStandManifestation();
            if (standManifestation instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) standManifestation;
                if (standEntity.isManuallyControlled() /*&& ((StandEntity) standManifestation).distanceToSqr(stand.getUser()) >= 25*/) {
                    boostRemoteControl = Math.min(boostRemoteControl + BOOST_REMOTE_PER_TICK, BOOST_REMOTE_MAX);
                    return;
                }
            }
        }
        boostRemoteControl = 1;
    }
    
    public void onChatMessage(String message) {
        if (boostAttack > 1 || hpOnGettingAttacked > -1) {
            int length = message.length();
            boostChat = Math.min(boostChat + length * BOOST_PER_CHARACTER, BOOST_CHAT_MAX);
            LivingEntity user = stand.getUser();
            if (user instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(new ResolveBoostsPacket(boostAttack, boostRemoteControl, boostChat, hpOnGettingAttacked), (ServerPlayerEntity) user);
            }
        }
    }

    public void onResolveEffectStarted(int amplifier) {
        int newLevel = amplifier + 1;
        stand.setResolveLevel(Math.min(newLevel, stand.getMaxResolveLevel()), true);
        if (newLevel > stand.getMaxResolveLevel()) {
            levels.incExtraLevel(stand);
        }
        setResolveValue(stand.getMaxResolve(), 0);
    }

    public void onResolveEffectEnded(int amplifier) {
        resetResolveValue();
    }
    
    
    
    public void soulAddResolveLook() {
        setResolveValue(getResolveValue() + getMaxResolveValue() / 60, -1);
    }
    
    public void soulAddResolveTeammate() {
        setResolveValue(getResolveValue() + getMaxResolveValue() / 300, -1);
    }
    

    
    public void setBoosts(float attack, float remoteControl, float chat) {
        this.boostAttack = attack;
        this.boostRemoteControl = remoteControl;
        this.boostChat = chat;
    }
    
    public void setHpOnAttack(float hp) {
        this.hpOnGettingAttacked = hp;
    }

    void reset() {
        resolve = 0;
        prevTickResolve = 0;
        levels.onStandSet(null);
        noResolveDecayTicks = 0;
        resolveRecords.clear();
        saveNextRecord = true;
        maxAchievedValue = 0;
        setBoosts(1, 1, 1);
        hpOnGettingAttacked = -1;
    }
    
    public void resetResolveValue() {
        resolve = 0;
        prevTickResolve = 0;
        noResolveDecayTicks = 0;
        resolveRecords.clear();
        saveNextRecord = true;
        maxAchievedValue = 0;
        setBoosts(1, 1, 1);
        hpOnGettingAttacked = -1;
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new ResetResolveValuePacket(), player);
        });
    }
    
    void clone(ResolveCounter previous) {
        this.resolve = previous.resolve;
        this.levels = previous.levels;
        this.noResolveDecayTicks = previous.noResolveDecayTicks;
        this.saveNextRecord = previous.saveNextRecord;
        this.resolveRecords = previous.resolveRecords;
        this.maxAchievedValue = previous.maxAchievedValue;
        this.boostAttack = previous.boostAttack;
        this.boostRemoteControl = previous.boostRemoteControl;
        this.boostChat = previous.boostChat;
        this.hpOnGettingAttacked = previous.hpOnGettingAttacked;
    }

    void alwaysResetOnDeath() {
        if (resolve > 0) {
            resolveRecords.add(resolve);
        }
        this.maxAchievedValue = Optional.ofNullable(resolveRecords.getMax()).orElse(0F);
        
        resolve = 0;
        prevTickResolve = 0;
        noResolveDecayTicks = 0;
        saveNextRecord = true;
        setBoosts(1, 1, 1);
        hpOnGettingAttacked = -1;
    }

    void syncWithUser(ServerPlayerEntity player) {
        PacketManager.sendToClient(new ResolvePacket(getResolveValue(), noResolveDecayTicks), player);
        PacketManager.sendToClient(new MaxAchievedResolvePacket(maxAchievedValue), player);
        PacketManager.sendToClient(new ResolveBoostsPacket(boostAttack, boostRemoteControl, boostChat, hpOnGettingAttacked), player);
    }

    void readNbt(CompoundNBT nbt) {
        resolve = nbt.getFloat("Resolve");
        prevTickResolve = resolve;
        noResolveDecayTicks = nbt.getInt("ResolveTicks");
        saveNextRecord = nbt.getBoolean("SaveNextRecord");
        boostAttack = nbt.getFloat("BoostAttack");
        boostRemoteControl = nbt.getFloat("BoostRemoteControl");
        boostChat = nbt.getFloat("BoostChat");
        hpOnGettingAttacked = nbt.getFloat("HpOnGettingAttacked");
        
        if (nbt.contains("Levels", MCUtil.getNbtId(CompoundNBT.class))) {
            levels.fromNBT(nbt.getCompound("Levels"));
        }
        
        else {
            LegacyUtil.readOldResolveLevels(nbt, levels, stand);
        }
        
        if (nbt.contains("ResolveRecord", MCUtil.getNbtId(ListNBT.class))) {
            ListNBT listNBT = nbt.getList("ResolveRecord", MCUtil.getNbtId(FloatNBT.class));
            for (int i = 0; i < listNBT.size(); i++) {
                this.resolveRecords.add(listNBT.getFloat(i));
            }
        }
    }

    CompoundNBT writeNBT() {
        CompoundNBT resolveNbt = new CompoundNBT();
        resolveNbt.putFloat("Resolve", resolve);
        resolveNbt.putInt("ResolveTicks", noResolveDecayTicks);
        resolveNbt.putBoolean("SaveNextRecord", saveNextRecord);
        resolveNbt.putFloat("BoostAttack", boostAttack);
        resolveNbt.putFloat("BoostRemoteControl", boostRemoteControl);
        resolveNbt.putFloat("BoostChat", boostChat);
        resolveNbt.putFloat("HpOnGettingAttacked", hpOnGettingAttacked);

        resolveNbt.put("Levels", levels.toNBT());
        
        ListNBT recordNbt = new ListNBT();
        for (float record : resolveRecords.getWrappedSet()) {
            recordNbt.add(FloatNBT.valueOf(record));
        }
        resolveNbt.put("ResolveRecord", recordNbt);
        
        return resolveNbt;
    }
}
