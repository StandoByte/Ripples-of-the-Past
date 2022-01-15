package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

// FIXME auto-summon stand
public class StarPlatinumZoom extends StandAction {

    public StarPlatinumZoom(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide) {
            ClientEventHandler.getInstance().isZooming = true;
        }
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, IStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.STAR_PLATINUM_ZOOM.get(), 1.0F, 1.0F, false, getPerformer(user, power), power, this);
        }
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, IStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide) {
            if (ticksHeld % 16 == 3 && ticksHeld > 32 && ticksHeld < 80) {
                LivingEntity stand = getPerformer(user, power);
                world.playSound(user instanceof PlayerEntity ? (PlayerEntity) user : null, 
                        stand.getX(), stand.getY(), stand.getZ(), ModSounds.STAR_PLATINUM_ZOOM_CLICK.get(), stand.getSoundSource(), 1.0F, 1.0F);
            }
        }
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld) {
        if (world.isClientSide) {
            ClientEventHandler.getInstance().isZooming = false;
        }
    }

}
