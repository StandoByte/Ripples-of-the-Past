package com.github.standobyte.jojo.util;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.command.ConfigPackCommand;
import com.github.standobyte.jojo.command.NonStandTypeArgument;
import com.github.standobyte.jojo.command.StandArgument;
import com.github.standobyte.jojo.init.ModPotions;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkillTree;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {
    
    @SubscribeEvent
    public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ForgeBusEventSubscriber.registerCapabilities();
            
            StandArgument.commonSetupRegister();
            NonStandTypeArgument.commonSetupRegister();

            ModCriteriaTriggers.CriteriaTriggerSupplier.registerAll();
            
            PacketManager.init();
            
            ConfigPackCommand.initConfigs(MinecraftForge.EVENT_BUS);
            
            // things to do after registry events
            ModPotions.registerRecipes();
            
            Action.initShiftVariations();
            for (AbstractHamonSkill hamonSkill : JojoCustomRegistries.HAMON_SKILLS.getRegistry()) {
                hamonSkill.onCommonSetup();
            }
            for (Action<?> action : JojoCustomRegistries.ACTIONS.getRegistry()) {
                action.onCommonSetup();
            }
            for (StandType<?> stand : JojoCustomRegistries.STANDS.getRegistry()) {
                stand.onCommonSetup();
            }
            
            BaseHamonSkillTree.initTrees();
        });
    }
}
