package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.world.World;

// TODO when the player walks a certain distance, add afterimages with the previous pose
// TODO color afterimages based on the pillar man's mode
public class PillarmanEvasion extends PillarmanAction {

    public PillarmanEvasion(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
    }
    
//    @Override
//    public void onHoldTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
//        if (world.isClientSide() && user == ClientUtil.getCameraEntity()) {
//            if (ticksHeld % 10 == 1) {
//                ClientEventHandler.getInstance().setDodgeCameraRoll(ticksHeld % 20 == 1 ? HandSide.LEFT : HandSide.RIGHT, 15, 10);
//            }
//        }
//    }

    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
//            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.PILLAR_MAN_EVASION.get(), 1.0F, 1.25F, true, user, power, this);
        }
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
        	/*user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
	            cap.addAfterimages(4, 40);
	        });*/
        }
    }
    
    @Override
    public int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return cooldownFromHoldDuration(super.getCooldownAdditional(power, ticksHeld), power, ticksHeld);
    }
    
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return ModPlayerAnimations.pillarmanEvasion.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        ModPlayerAnimations.pillarmanEvasion.setAnimEnabled(user, false);
    }
}
