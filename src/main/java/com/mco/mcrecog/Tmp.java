package com.mco.mcrecog;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class Tmp {

    @SubscribeEvent
    public static void onChat(ClientChatEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        IStandPower stand = IStandPower.getStandPowerOptional(mc.player).orElse(null);
        INonStandPower power = INonStandPower.getNonStandPowerOptional(mc.player).orElse(null);
        if ((stand == null || !stand.hasPower()) && (power == null || !power.hasPower())) return;
        
        String msg = event.getMessage().replace(" ", "");
        MCRecog.handleCommand(msg, stand, power, false);
        
    }
}
