package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.damaging.projectile.PillarmanDivineSandstormEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanAtmosphericRift extends PillarmanDivineSandstorm {

    public PillarmanAtmosphericRift(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.WIND;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
        	if(ticksHeld < 40) {
        		auraEffect(user, ModParticles.HAMON_AURA_GREEN.get(), 3);
        	} else {
        		for (int i = 0; i < 3; i++) {
                    Vector3d particlePos = user.position().add(
                            (Math.random() - 0.5) * (user.getBbWidth() + 0.5), 
                            Math.random() * (user.getBbHeight()), 
                            (Math.random() - 0.5) * (user.getBbWidth() + 0.5));
                    user.level.addParticle(ModParticles.BLOOD.get(), particlePos.x, particlePos.y, particlePos.z, 
                    		(Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2);
                }
        	}
        }
    }
    
    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        int maxTicks = Math.max(getHoldDurationToFire(power), 1);
        int ticksHeld = Math.min(power.getHeldActionTicks(), maxTicks);
        if (ticksHeld >= maxTicks) {
            return 3.0F;
        }
        return 0;
    }

    public static final DamageSource FINAL_MODE_SELF_DAMAGE = (new DamageSource("generic")).bypassArmor(); // TODO separate msgId & death message in lang files
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide()) {
            int maxTicks = Math.max(getHoldDurationToFire(power), 1);
            if (ticksHeld >= maxTicks && power.getEnergy() > 0 && ticksHeld % 2 == 0) {
                PillarmanDivineSandstormEntity sanstormWave = new PillarmanDivineSandstormEntity(world, user, 0)
                		.setAtmospheric(true)
                        .setRadius(0.5F)
                        .setDamage(2F)
                        .setDuration(60);
                sanstormWave.shootFromRotation(user, 1.75F, 1F);
                world.addFreshEntity(sanstormWave);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 
                        SoundCategory.AMBIENT, 0.1F, 1.0F);
                PlayerEntity playerentity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
                if (playerentity == null || !playerentity.abilities.instabuild) {
                    user.hurt(FINAL_MODE_SELF_DAMAGE, 1F);
                }
            }
        }
    }

    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return ModPlayerAnimations.atmosphericRift.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        ModPlayerAnimations.atmosphericRift.setAnimEnabled(user, false);
    }
}
