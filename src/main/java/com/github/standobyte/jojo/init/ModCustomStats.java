package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.stats.IStatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCustomStats {
    public static final ResourceLocation VAMPIRE_PEOPLE_DRAINED = new ResourceLocation(JojoMod.MOD_ID, "vampire_people_drained");
    public static final ResourceLocation VAMPIRE_ANIMALS_DRAINED = new ResourceLocation(JojoMod.MOD_ID, "vampire_animals_drained");
    public static final ResourceLocation VAMPIRE_ZOMBIES_CREATED = new ResourceLocation(JojoMod.MOD_ID, "vampire_zombies_created");
    public static final ResourceLocation VAMPIRE_ZOMBIES_SUMMONED = new ResourceLocation(JojoMod.MOD_ID, "vampire_zombies_summoned");
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void registerCustomStats(RegistryEvent.Register<StatType<?>> event) {
        registerCustomStat(VAMPIRE_PEOPLE_DRAINED, IStatFormatter.DEFAULT);
        registerCustomStat(VAMPIRE_ANIMALS_DRAINED, IStatFormatter.DEFAULT);
        registerCustomStat(VAMPIRE_ZOMBIES_CREATED, IStatFormatter.DEFAULT);
        registerCustomStat(VAMPIRE_ZOMBIES_SUMMONED, IStatFormatter.DEFAULT);
    }

    private static ResourceLocation registerCustomStat(ResourceLocation resLoc, IStatFormatter statFormatter) {
        Registry.register(Registry.CUSTOM_STAT, resLoc, resLoc);
        Stats.CUSTOM.get(resLoc, statFormatter);
        return resLoc;
    }
}
