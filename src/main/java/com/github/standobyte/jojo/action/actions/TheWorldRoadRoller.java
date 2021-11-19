package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class TheWorldRoadRoller extends StandEntityAction {

    public TheWorldRoadRoller(Builder builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            RoadRollerEntity roadRoller = new RoadRollerEntity(world);
            roadRoller.copyPosition(user);
            world.addFreshEntity(roadRoller);
            user.startRiding(roadRoller);
        }
    }
    
    @Override
    public int getCooldown(IPower<?> power, int ticksHeld) {
        LivingEntity user = power.getUser();
        if (user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild) {
            return 0;
        }
        return super.getCooldown(power, ticksHeld);
    }
}
