package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class PillarmanUnnaturalAgility extends PillarmanAction {

    public PillarmanUnnaturalAgility(PillarmanAction.Builder builder) {
        super(builder.holdType());
        stage = 2;
    }

//    @Override
//    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
//        
//    }
    
    public static boolean onUserAttacked(LivingAttackEvent event) {
        Entity attacker = event.getSource().getDirectEntity();
        if (attacker instanceof LivingEntity && !attacker.isOnFire() && !DamageUtil.isImmuneToCold(attacker)) {
            LivingEntity targetLiving = event.getEntityLiving();
            return INonStandPower.getNonStandPowerOptional(targetLiving).map(power -> {
                if (power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_UNNATURAL_AGILITY.get()) {
                    World world = attacker.level;
                    world.playSound(null, attacker, ModSounds.HAMON_SYO_SWING.get(), attacker.getSoundSource(), 1.0F, 1.0F); // TODO separate sound event
                    return true;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.HAMON_SYO_SWING.get(), 1.0F, 1.25F, true, user, power, this); // TODO separate sound event
        }
    }
    
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return ModPlayerAnimations.unnaturalAgility.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        ModPlayerAnimations.unnaturalAgility.setAnimEnabled(user, false);
    }
}
