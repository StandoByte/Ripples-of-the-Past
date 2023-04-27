package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCommonRegistries {
    
    public static final CustomRegistryHolder<Action<?>> ACTIONS = new CustomRegistryHolder<>(
            DeferredRegister.create((Class<Action<?>>) ((Class<?>) Action.class), JojoMod.MOD_ID), "action");
    
    public static final CustomRegistryHolder<NonStandPowerType<?>> NON_STAND_POWERS = new CustomRegistryHolder<>(
            DeferredRegister.create((Class<NonStandPowerType<?>>) ((Class<?>) NonStandPowerType.class), JojoMod.MOD_ID), "non_stand_type");

    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeActionsInit(RegistryEvent.Register<Action<?>> event) {
        Action.prepareShiftVariationsMap();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void afterActionsInit(RegistryEvent.Register<Action<?>> event) {
        Action.initShiftVariations();
    }
}
