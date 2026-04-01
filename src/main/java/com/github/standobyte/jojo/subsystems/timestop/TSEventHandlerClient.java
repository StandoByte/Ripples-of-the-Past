package com.github.standobyte.jojo.subsystems.timestop;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class TSEventHandlerClient {

    @SubscribeEvent
    public static void decreaseEntityTickCount(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        World world = ClientUtil.getClientWorld();
        if (world != null) {
            for (Entity entity : MCUtil.getAllEntities(world)) {
                if (!entity.canUpdate()) {
                    entity.tickCount--;
                }
            }
        }
    }
}
