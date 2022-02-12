package com.github.standobyte.jojo.power.stand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.init.ModActions;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class ActionLearningProgressMap {
    private final Map<Action<IStandPower>, Float> wrappedMap = new HashMap<>();

    float getLearningProgress(Action<IStandPower> action) {
        return wrappedMap.getOrDefault(action, -1F);
    }
    
    boolean setLearningProgress(Action<IStandPower> action, float progress) {
        if (wrappedMap.containsKey(action) && wrappedMap.get(action) == progress) {
            return false;
        }
        wrappedMap.put(action, progress);
        return true;
    }
    
    void forEach(BiConsumer<Action<IStandPower>, Float> consumer) {
        wrappedMap.forEach(consumer);
    }
    
    void readFromNbt(CompoundNBT cnbt) {
        if (cnbt.contains("ActionLearning", 10)) {
            CompoundNBT nbt = cnbt.getCompound("ActionLearning");
            nbt.getAllKeys().forEach(actionName -> {
                Action<?> action = ModActions.Registry.getRegistry().getValue(new ResourceLocation(actionName));
                if (action instanceof StandAction && nbt.contains(actionName, 5)) {
                    wrappedMap.put((Action<IStandPower>) action, nbt.getFloat(actionName));
                }
            });
        }
    }
    
    void writeToNbt(CompoundNBT cnbt) {
        CompoundNBT nbt = new CompoundNBT();
        forEach((action, progress) -> {
            nbt.putFloat(action.getRegistryName().toString(), progress);
        });
        cnbt.put("ActionLearning", nbt);
    }
}
