package com.github.standobyte.jojo.power.stand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.power.ModCommonRegistries;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ActionLearningProgressMap<P extends IPower<P, ?>> {
    private final Map<Action<P>, Float> wrappedMap = new HashMap<>();
    
    boolean hasEntry(Action<P> action) {
        return wrappedMap.containsKey(action);
    }

    float getLearningProgressPoints(Action<P> action, P power, boolean maxLimit) {
        if (!hasEntry(action)) {
            return -1F;
        }
        return maxLimit ? MathHelper.clamp(wrappedMap.get(action), 0F, action.getMaxTrainingPoints(power))
                : Math.max(wrappedMap.get(action), 0F);
    }
    
    public boolean setLearningProgressPoints(Action<P> action, float progress, P power) {
        if (wrappedMap.containsKey(action) && wrappedMap.get(action) == progress) {
            return false;
        }
        wrappedMap.put(action, progress);
        return true;
    }
    
    void forEach(BiConsumer<Action<P>, Float> consumer) {
        wrappedMap.forEach(consumer);
    }
    
    void fromNBT(CompoundNBT nbt) {
        nbt.getAllKeys().forEach(actionName -> {
            Action<?> action = ModCommonRegistries.ACTIONS.getRegistry().getValue(new ResourceLocation(actionName));
            if (action instanceof StandAction && nbt.contains(actionName, 5)) {
                wrappedMap.put((Action<P>) action, nbt.getFloat(actionName));
            }
        });
    }
    
    CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        forEach((action, progress) -> {
            nbt.putFloat(action.getRegistryName().toString(), progress);
        });
        return nbt;
    }
}
