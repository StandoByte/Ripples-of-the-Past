package com.github.standobyte.jojo.util;

import java.util.Map;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.init.ModActions;
import com.google.common.collect.ImmutableMap;

public class LegacyUtil {
    private static final Map<Supplier<? extends StandAction>, Integer> EXP_REQUIREMENT = 
            new ImmutableMap.Builder<Supplier<? extends StandAction>, Integer>()
/*1*/        .put(ModActions.STAR_PLATINUM_STAR_FINGER, 300)
/*4*/        .put(ModActions.STAR_PLATINUM_TIME_STOP, 950)
/*4*/        .put(ModActions.STAR_PLATINUM_TIME_STOP_BLINK, 950)
/*1*/        .put(ModActions.THE_WORLD_TIME_STOP, 500)
/*1*/        .put(ModActions.THE_WORLD_TIME_STOP_BLINK, 500)
/*0*/        .put(ModActions.HIEROPHANT_GREEN_STRING_BIND, 200)
/*1*/        .put(ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH, 50)
/*1+*/       .put(ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED, 400)
/*1*/        .put(ModActions.HIEROPHANT_GREEN_GRAPPLE, 100)
/*1*/        .put(ModActions.HIEROPHANT_GREEN_GRAPPLE_ENTITY, 100)
/*3*/        .put(ModActions.HIEROPHANT_GREEN_BARRIER, 700)
/*1*/        .put(ModActions.SILVER_CHARIOT_RAPIER_LAUNCH, 200)
/*3*/        .put(ModActions.SILVER_CHARIOT_TAKE_OFF_ARMOR, 600)
/*0*/        .put(ModActions.MAGICIANS_RED_FLAME_BURST, 50)
/*1*/        .put(ModActions.MAGICIANS_RED_FIREBALL, 150)
/*4*/        .put(ModActions.MAGICIANS_RED_CROSSFIRE_HURRICANE, 700)
/*4+*/       .put(ModActions.MAGICIANS_RED_CROSSFIRE_HURRICANE_SPECIAL, 1000)
/*3*/        .put(ModActions.MAGICIANS_RED_RED_BIND, 450)
/*2*/        .put(ModActions.MAGICIANS_RED_DETECTOR, 500)
            .build();
}
