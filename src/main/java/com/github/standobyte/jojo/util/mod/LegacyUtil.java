package com.github.standobyte.jojo.util.mod;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.ResolveLevelsMap;
import com.github.standobyte.jojo.power.impl.stand.StandActionLearningProgress;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class LegacyUtil {
    
    public static Optional<StandInstance> readOldStandCapType(CompoundNBT capNbt) {
        if (capNbt.contains("StandType", MCUtil.getNbtId(StringNBT.class))) {
            String standName = capNbt.getString("StandType");
            if (standName != IPowerType.NO_POWER_NAME) {
                StandType<?> stand = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standName));
                return Optional.ofNullable(new StandInstance(stand));
            }
        }
        return Optional.empty();
    }
    
    public static void readOldResolveLevels(CompoundNBT mainCounterNBT, ResolveLevelsMap levelsMap, IStandPower standPower) {
        int resolveLevel = mainCounterNBT.getByte("ResolveLevel");
        int extraLevel = mainCounterNBT.getInt("ExtraLevel");
        
        levelsMap.readOldValues(standPower, resolveLevel, extraLevel);
    }
    
    public static Optional<StandActionLearningProgress.StandActionLearningEntry> readOldStandActionLearning(CompoundNBT mainNbt, String key) {
        IForgeRegistry<Action<?>> actions = JojoCustomRegistries.ACTIONS.getRegistry();
        ResourceLocation keyResLoc = new ResourceLocation(key);
        if (!actions.containsKey(keyResLoc)) {
            return Optional.empty();
        }
        Action<?> action = actions.getValue(keyResLoc);
        if (!(action instanceof StandAction)) {
            return Optional.empty();
        }

        StandType<?> standType = null;
        String[] actionNameSplit = keyResLoc.getPath().split("_");
        if (actionNameSplit.length > 1) {
            ResourceLocation standIdGuess = new ResourceLocation(keyResLoc.getNamespace(), actionNameSplit[0] + "_" + actionNameSplit[1]);
            IForgeRegistry<StandType<?>> stands = JojoCustomRegistries.STANDS.getRegistry();
            if (stands.containsKey(standIdGuess)) {
                standType = stands.getValue(standIdGuess);
            }
        }
        if (standType == null) {
            return Optional.empty();
        }
        
        return Optional.of(new StandActionLearningProgress.StandActionLearningEntry((StandAction) action, standType, mainNbt.getFloat(key)));
    }
}
