package com.github.standobyte.jojo.power.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEffectPacket;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class StandEffectsTracker {
    private static final AtomicInteger EFFECTS_COUNTER = new AtomicInteger();
    private final IStandPower standPower;
    private final Int2ObjectMap<StandEffectInstance> effects = new Int2ObjectLinkedOpenHashMap<>();
    
    public StandEffectsTracker(IStandPower standPower) {
        this.standPower = standPower;
    }
    
    public void addEffect(StandEffectInstance instance) {
        LivingEntity user = standPower.getUser();
        if (!user.level.isClientSide()) {
            instance.withId(EFFECTS_COUNTER.incrementAndGet());
        }
        putEffectInstance(instance);
        if (!user.level.isClientSide()) {
            instance.syncWithTrackingAndUser();
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
    public <T extends StandEffectInstance> T getOrCreateEffect(StandEffectType<T> effectType, LivingEntity target) {
        List<StandEffectInstance> effectList = getEffects(effect -> 
                effect.effectType == effectType && 
                (target == null ? effect.getTargetUUID() == null : target.getUUID().equals(effect.getTargetUUID())));
        if (effectList.isEmpty()) {
            T effect = effectType.create();
            addEffect(effect.withTarget(target));
            return effect;
        }
        else {
            return (T) effectList.get(0);
        }
    }
    
    public List<StandEffectInstance> getEffects(@Nullable Predicate<StandEffectInstance> filter) {
        if (filter == null) {
            return new ArrayList<>(effects.values());
        }
        return effects.values().stream().filter(filter).collect(Collectors.toList());
    }
    
    public void syncWithUserOnly(ServerPlayerEntity user) {
        effects.values().forEach(effect -> {
            effect.syncWithUserOnly(user);
        });
    }
    
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        effects.values().forEach(effect -> {
            effect.updateTarget(player.getLevel());
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
        if (nbt.contains("Effects", JojoModUtil.getNbtId(ListNBT.class))) {
            nbt.getList("Effects", JojoModUtil.getNbtId(CompoundNBT.class)).forEach(effectNBT -> {
                StandEffectInstance effect = StandEffectInstance.fromNBT((CompoundNBT) effectNBT);
                if (effect != null) {
                    putEffectInstance(effect.withId(EFFECTS_COUNTER.incrementAndGet()));
                }
            });
        }
    }
}
