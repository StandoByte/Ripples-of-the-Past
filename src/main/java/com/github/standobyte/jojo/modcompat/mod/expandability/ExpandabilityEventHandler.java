package com.github.standobyte.jojo.modcompat.mod.expandability;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import be.florens.expandability.api.forge.LivingFluidCollisionEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ExpandabilityEventHandler {

    @SubscribeEvent
    public void liquidWalking(LivingFluidCollisionEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            FluidState fluid = event.getFluidState();
            if (HamonUtil.onLiquidWalkingEvent(entity, fluid)) {
                event.setResult(Event.Result.ALLOW);
            }
        }
    }
}
