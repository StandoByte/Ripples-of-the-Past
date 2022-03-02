package com.github.standobyte.jojo.power.stand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ActionLearningProgressMap<P extends IPower<P, ?>> {
    private final Map<Action<P>, Float> wrappedMap = new HashMap<>();

    float getLearningProgressPoints(Action<P> action, P power) {
        if (!wrappedMap.containsKey(action)) {
            return -1F;
        }
        return MathHelper.clamp(wrappedMap.get(action), 0F, action.getMaxTrainingPoints(power));
    }
    
    boolean setLearningProgressPoints(Action<P> action, float progress, P power) {
        if (wrappedMap.containsKey(action) && wrappedMap.get(action) == progress) {
            return false;
        }
        wrappedMap.put(action, progress);
        return true;
    }
    
    void forEach(BiConsumer<Action<P>, Float> consumer) {
        wrappedMap.forEach(consumer);
    }
    
    void readFromNbt(CompoundNBT cnbt) {
        if (cnbt.contains("ActionLearning", 10)) {
            CompoundNBT nbt = cnbt.getCompound("ActionLearning");
            nbt.getAllKeys().forEach(actionName -> {
                Action<?> action = ModActions.Registry.getRegistry().getValue(new ResourceLocation(actionName));
                if (action instanceof StandAction && nbt.contains(actionName, 5)) {
                    wrappedMap.put((Action<P>) action, nbt.getFloat(actionName));
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
