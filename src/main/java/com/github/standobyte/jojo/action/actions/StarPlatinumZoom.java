package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class StarPlatinumZoom extends StandEntityAction {

    public StarPlatinumZoom(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        super.startedHolding(world, user, power, target, requirementsFulfilled);
        if (world.isClientSide) {
            ClientEventHandler.getInstance().isZooming = true;
        }
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, IStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.STAR_PLATINUM_ZOOM.get(), 
                    1.0F, 1.0F, false, (StandEntity) power.getStandManifestation(), power, this);
        }
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (world.isClientSide) {
            if (ticks % 16 == 3 && ticks > 32 && ticks < 80) {
                PlayerEntity player = ClientUtil.getClientPlayer();
                if (player.is(standEntity.getUser())) {
                    world.playSound(player, standEntity.getX(), standEntity.getY(), standEntity.getZ(), 
                            ModSounds.STAR_PLATINUM_ZOOM_CLICK.get(), standEntity.getSoundSource(), 1.0F, 1.0F);
                }
            }
        }
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld) {
        super.stoppedHolding(world, user, power, ticksHeld);
        if (world.isClientSide) {
            ClientEventHandler.getInstance().isZooming = false;
        }
    }

}
