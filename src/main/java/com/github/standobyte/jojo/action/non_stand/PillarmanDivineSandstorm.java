package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.damaging.projectile.PillarmanDivineSandstormEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class PillarmanDivineSandstorm extends PillarmanAction {

    public PillarmanDivineSandstorm(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.WIND;
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getHeldAction() == this) {
            if (power.getEnergy() == 0F) {
                return conditionMessage("no_energy_pillarman");
            }
        }
        return ActionConditionResult.POSITIVE;
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
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled && ticksHeld < 40) {
            auraEffect(user, ModParticles.HAMON_AURA_GREEN.get(), 6);
        }
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide()) {
            int maxTicks = Math.max(getHoldDurationToFire(power), 1);
            if (ticksHeld >= maxTicks && power.getEnergy() > 0 && ticksHeld % 2 == 0) {
                PillarmanDivineSandstormEntity sanstormWave = new PillarmanDivineSandstormEntity(world, user, 0)
                		.setAtmospheric(false)
                        .setRadius(1.5F)
                        .setDamage(2F)
                        .setDuration(10);
                sanstormWave.shootFromRotation(user, 0.9F, 2F);
                world.addFreshEntity(sanstormWave);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 
                        SoundCategory.AMBIENT, 0.2F, 1.0F);
                /*PillarmanDivineSandstormEntity sanstormWave2 = new PillarmanDivineSandstormEntity(world, user, -2F)
                        .setRadius(1.5F)
                        .setDamage(1F)
                        .setDuration(10);
                sanstormWave2.shootFromRotation(user, 0.9F, 2F);
                world.addFreshEntity(sanstormWave2);*/
            }
        }
    }
    
    public static void auraEffect(LivingEntity user, IParticleData particles, int intensity) {
        if (user.level.isClientSide()) {
            boolean isUserTheCameraEntity = user == ClientUtil.getCameraEntity();
            for (int i = 0; i < intensity; i++) {
                CustomParticlesHelper.createHamonAuraParticle(particles, user, 
                        user.getX() + (Math.random() - 0.5) * (user.getBbWidth() + 0.5F), 
                        user.getY() + Math.random() * (user.getBbHeight() * 0.5F), 
                        user.getZ() + (Math.random() - 0.5) * (user.getBbWidth() + 0.5F));
            }
            if (isUserTheCameraEntity) {
                CustomParticlesHelper.summonHamonAuraParticlesFirstPerson(particles, user, intensity / 5);
            }
        }
    }
    
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return ModPlayerAnimations.divineSandstorm.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        ModPlayerAnimations.divineSandstorm.setAnimEnabled(user, false);
    }
}
