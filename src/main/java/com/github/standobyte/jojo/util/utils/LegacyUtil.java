package com.github.standobyte.jojo.util.utils;

import java.util.Arrays;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.ActionLearningProgressMap;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.google.common.collect.Streams;

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
}
