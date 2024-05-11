package com.github.standobyte.jojo.power.impl.stand;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEffectPacket;
import com.github.standobyte.jojo.util.mc.MCUtil;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.vector.Vector3d;

public class StandEffectsTracker {
    public static final AtomicInteger EFFECTS_COUNTER = new AtomicInteger();
    private IStandPower standPower;
    private final Int2ObjectMap<StandEffectInstance> effects = new Int2ObjectLinkedOpenHashMap<>();
    
    public StandEffectsTracker(IStandPower standPower) {
        this.standPower = standPower;
    }
    
    public void setPowerData(IStandPower standPower) {
        this.standPower = standPower;
        effects.values().forEach(effect -> effect.withStand(standPower));
    }
    
    public void addEffect(StandEffectInstance instance) {
        LivingEntity user = standPower.getUser();
        if (!user.level.isClientSide()) {
            instance.withId(EFFECTS_COUNTER.incrementAndGet());
        }
        putEffectInstance(instance);
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTracking(TrStandEffectPacket.add(instance, false), user);
            if (user instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(TrStandEffectPacket.add(instance, true), (ServerPlayerEntity) user);
            }
        }
    }
    
    private void putEffectInstance(StandEffectInstance instance) {
        instance.withStand(standPower);
        effects.put(instance.getId(), instance);
        instance.onStart();
    }
    
    public void removeEffect(StandEffectInstance instance) {
        if (instance != null) {
            onEffectRemoved(instance);
            effects.remove(instance.getId());
        }
    }

    public void tick() {
        if (effects.isEmpty()) {
            return;
        }
        
        ObjectIterator<Entry<StandEffectInstance>> it = effects.int2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            StandEffectInstance effect = it.next().getValue();
            if (!effect.toBeRemoved()) {
                effect.onTick();
            }
            if (effect.toBeRemoved()) {
                onEffectRemoved(effect);
                it.remove();
            }
        }
    }
    
    public void onStandUserDeath(LivingEntity user) {
        ObjectIterator<Entry<StandEffectInstance>> it = effects.int2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            StandEffectInstance effect = it.next().getValue();
            if (effect.removeOnUserDeath()) {
                onEffectRemoved(effect);
                it.remove();
            }
        }
    }
    
    public void onStandUserLogout(ServerPlayerEntity user) {
        if (!user.server.isPublished()) return;
        
        ObjectIterator<Entry<StandEffectInstance>> it = effects.int2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            StandEffectInstance effect = it.next().getValue();
            if (effect.removeOnUserLogout()) {
                onEffectRemoved(effect);
                it.remove();
            }
        }
    }
    
    public void onStandChanged(LivingEntity user) {
        ObjectIterator<Entry<StandEffectInstance>> it = effects.int2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            StandEffectInstance effect = it.next().getValue();
            if (effect.removeOnStandChanged()) {
                onEffectRemoved(effect);
                it.remove();
            }
        }
    }
    
    private void onEffectRemoved(StandEffectInstance instance) {
        instance.onStop();
        LivingEntity user = standPower.getUser();
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(TrStandEffectPacket.remove(instance), user);
        }
    }

//    public void onUserStandRemoved(LivingEntity user) {
//        effects.values().forEach(effect -> effect.onStop());
//        effects.clear();
//        if (!user.level.isClientSide()) {
//            PacketManager.sendToClientsTrackingAndSelf(TrStandEffectPacket.removeAll(user), user);
//        }
//    }
    
    public StandEffectInstance getById(int id) {
        return effects.get(id);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends StandEffectInstance> Optional<T> getEffectTargeting(StandEffectType<T> effectType, LivingEntity target) {
        Stream<StandEffectInstance> effects = getEffects().filter(effect -> 
                effect.effectType == effectType && 
                (target == null ? effect.getTargetUUID() == null : target.getUUID().equals(effect.getTargetUUID())));
        Optional<T> effect = effects.findFirst().map(e -> (T) e);
        return effect;
    }
    
    public <T extends StandEffectInstance> T getOrCreateEffect(StandEffectType<T> effectType, LivingEntity target) {
        Optional<T> effect = getEffectTargeting(effectType, target);
        if (effect.isPresent()) {
            return effect.get();
        }
        else {
            T newEffect = effectType.create();
            addEffect(newEffect.withTarget(target));
            return newEffect;
        }
    }
    
    public Stream<StandEffectInstance> getEffects() {
        return effects.values().stream();
    }
    
    public void syncWithUserOnly(ServerPlayerEntity user) {
        effects.values().forEach(effect -> {
            effect.syncWithUserOnly(user);
        });
    }
    
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        effects.values().forEach(effect -> {
            effect.syncWithTrackingOrUser(player);
        });
    }
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT effectsList = new ListNBT();
        effects.forEach((id, effect) -> {
            if (!effect.toBeRemoved()) {
                effectsList.add(effect.toNBT());
            }
        });
        nbt.put("Effects", effectsList);
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("Effects", MCUtil.getNbtId(ListNBT.class))) {
            nbt.getList("Effects", MCUtil.getNbtId(CompoundNBT.class)).forEach(effectNBT -> {
                StandEffectInstance effect = StandEffectInstance.fromNBT((CompoundNBT) effectNBT);
                if (effect != null) {
                    putEffectInstance(effect.withId(EFFECTS_COUNTER.incrementAndGet()));
                }
            });
        }
    }
    
    
    
    public static <T extends StandEffectInstance> Stream<T> getEffectsOfType(IStandPower power, StandEffectType<T> type, double range) {
        double rangeSq = range * range;
        return power.getContinuousEffects()
                .getEffects()
                .filter(effect -> effect.effectType == type)
                .filter(effect -> effect.getTarget() != null
                        && (range <= 0 || effect.getTarget().distanceToSqr(power.getUser()) < rangeSq))
                .map(effect -> (T) effect);
    }
    
    /**
     * @return Optional of stream with StandEffectInstance of that type. Instead of an empty stream returns empty optional.
     */
    public static <T extends StandEffectInstance> Stream<T> getEffectsTargetedBy(LivingEntity entity, StandEffectType<T> type) {
        return entity.getCapability(LivingUtilCapProvider.CAPABILITY).resolve().map(cap -> cap.getEffectsTargetedBy().stream()
                        .filter(effect -> effect.effectType == type)
                        .map(effect -> (T) effect))
                .orElse(Stream.empty());
    }
    
    public static boolean isTargetedBy(LivingEntity entity, StandEffectType<?> type) {
        return getEffectsTargetedBy(entity, type).findAny().isPresent();
    }
    
    public static Optional<StandEffectInstance> getTargetLookedAt(Stream<? extends StandEffectInstance> targets, LivingEntity user) {
        Vector3d lookAngle = user.getLookAngle();
        Vector3d eyePos = user.getEyePosition(1.0F);
        return targets.max(Comparator.comparingDouble(
                e -> lookAngle.dot(e.getTarget().getBoundingBox().getCenter().subtract(eyePos).normalize())))
                .map(Function.identity());
    }
    
    public static Optional<StandEffectInstance> getTargetLookedAt(IStandPower power, StandEffectType<?> type, double range, LivingEntity user) {
        return getTargetLookedAt(getEffectsOfType(power, type, range), user);
    }
}
