package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.PillarmanDivineSandstormEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;

public class PillarmanAtmosphericRift extends PillarmanDivineSandstorm {

    public PillarmanAtmosphericRift(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.WIND;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            auraEffect(user, ModParticles.HAMON_AURA_GREEN.get(), 12);
            auraEffect(user, ParticleTypes.LAVA, 1);
        }
    }
    
    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        int maxTicks = Math.max(getHoldDurationToFire(power), 1);
        int ticksHeld = Math.min(power.getHeldActionTicks(), maxTicks);
        if (ticksHeld >= maxTicks) {
            return 5.0F;
        }
        return 0;
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide()) {
            int maxTicks = Math.max(getHoldDurationToFire(power), 1);
            if (ticksHeld >= maxTicks && power.getEnergy() > 0) {
                PillarmanDivineSandstormEntity sanstormWave = new PillarmanDivineSandstormEntity(world, user)
                        .setRadius(0.5F)
                        .setDamage(2F)
                        .setDuration(60);
                sanstormWave.shootFromRotation(user, 0.9F, 1F);
                world.addFreshEntity(sanstormWave);
                PlayerEntity playerentity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
                if (playerentity == null || !playerentity.abilities.instabuild) {
                    user.hurt(EntityDamageSource.GENERIC, 2F); // TODO separate DamageSource with a death message
                }
            }
        }
    }

    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return false;
    }
}
