package com.github.standobyte.jojo.util.utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.stand.ActionLearningProgressMap;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.google.common.collect.Streams;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public class LegacyUtil {
    
    public static void readNbtStandXp(IStandPower power, int xpValue, ActionLearningProgressMap<IStandPower> unlockedActions) {
        if (power.hasPower()) {
            StandType<?> stand = power.getType();
            Streams.concat(Arrays.stream(stand.getAttacks()), Arrays.stream(stand.getAbilities()))
            .flatMap(action -> action.hasShiftVariation() && action.getShiftVariationIfPresent() instanceof StandAction
                    ? Stream.of(action, (StandAction) action.getShiftVariationIfPresent()) : Stream.of(action))
            .filter(action -> action.canBeUnlocked(power) || action.getXpRequirement() <= xpValue)
            .forEach(action -> unlockedActions.setLearningProgressPoints(action, getLearningFromXp(action, xpValue, power), power));
        }
    }
    
    private static float getLearningFromXp(StandAction action, int xpValue, IStandPower power) {
        int min = -1;
        int max = -1;
        float ptsRatio = 1;
        if (action == ModActions.STAR_PLATINUM_TIME_STOP.get() || action == ModActions.STAR_PLATINUM_TIME_STOP_BLINK.get() || 
                action == ModActions.THE_WORLD_TIME_STOP.get() || action == ModActions.THE_WORLD_TIME_STOP_BLINK.get()) {
            min = action.getXpRequirement();
            max = 1000;
        }
        if (action == ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH.get()) {
            min = ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH.get().getXpRequirement();
            max = ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get().getXpRequirement();
        }
        if (action == ModActions.MAGICIANS_RED_CROSSFIRE_HURRICANE.get()) {
            min = ModActions.MAGICIANS_RED_CROSSFIRE_HURRICANE.get().getXpRequirement();
            max = ModActions.MAGICIANS_RED_CROSSFIRE_HURRICANE_SPECIAL.get().getXpRequirement();
        }
        if (min > -1 && max > -1) {
            ptsRatio = (Math.min(xpValue, max) - min) / (max - min);
        }
        return action.getMaxTrainingPoints(power) * ptsRatio;
    }
    
    // TODO remove 'clientSide' params from StandDiscItem methods
    public static Optional<StandInstance> oldStandDiscInstance(ItemStack disc, boolean clientSide) {
        CompoundNBT nbt = disc.getTag();
        if (nbt.contains("Stand", JojoModUtil.getNbtId(StringNBT.class))) {
            StandType<?> standType = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(nbt.getString("Stand")));
            if (standType != null) {
                StandInstance standInstance = new StandInstance(standType);
                if (!clientSide) {
                    nbt.put("Stand", standInstance.writeNBT());
                }
                return Optional.of(standInstance);
            }
        }
        return Optional.empty();
    }
    
    public static Optional<StandInstance> readOldStandCapType(CompoundNBT capNbt) {
        if (capNbt.contains("StandType", JojoModUtil.getNbtId(StringNBT.class))) {
            String standName = capNbt.getString("StandType");
            if (standName != IPowerType.NO_POWER_NAME) {
                StandType<?> stand = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standName));
                return Optional.ofNullable(new StandInstance(stand));
            }
        }
        return Optional.empty();
    }
}
