package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
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
    
    private static boolean canSeeStands(LivingEntity user) {
        return StandUtil.isEntityStandUser(user) || user.hasEffect(ModStatusEffects.SPIRIT_VISION.get());
    }
    
    public static boolean onUserAttacked(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getDirectEntity();
        if (!source.isExplosion() && (attacker instanceof LivingEntity || attacker instanceof ProjectileEntity)) {
            LivingEntity targetLiving = event.getEntityLiving();
            return INonStandPower.getNonStandPowerOptional(targetLiving).map(power -> {
                Action<?> heldAction = power.getHeldAction(true);
                if (heldAction == ModPillarmanActions.PILLARMAN_UNNATURAL_AGILITY.get() 
                		|| heldAction == ModPillarmanActions.PILLARMAN_EVASION.get()) {
                    World world = attacker.level;
                    if (attacker instanceof StandEntity && !canSeeStands(targetLiving)) {
                        return false;
                    }
                    if (attacker instanceof ModdedProjectileEntity) {
                        ModdedProjectileEntity projectile = (ModdedProjectileEntity) attacker;
                        return projectile.canBeEvaded(targetLiving) && (!projectile.standDamage() || canSeeStands(targetLiving));
                	}
                    if (power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_UNNATURAL_AGILITY.get() 
                    		&& attacker instanceof LivingEntity && !(attacker instanceof StandEntity)) {
                    	double counterAttack = Math.random();
                    	if (counterAttack < 0.3) {
                    		attacker.hurt(EntityDamageSource.playerAttack((PlayerEntity) targetLiving), 
	                            (DamageUtil.getDamageWithoutHeldItem(targetLiving) * 0.75F));
                    	}
                    }
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
