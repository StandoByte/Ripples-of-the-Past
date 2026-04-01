package com.github.standobyte.jojo.subsystems.timestop;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TSEventHandler {

    @SubscribeEvent
    public static void decreaseEntityTickCount(WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        World world = event.world;
        for (Entity entity : MCUtil.getAllEntities(world)) {
            if (!entity.canUpdate()) {
                entity.tickCount--;
            }
        }
    }
}
