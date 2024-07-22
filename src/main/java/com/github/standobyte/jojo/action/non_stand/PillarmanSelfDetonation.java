package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion.CustomExplosionType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class PillarmanSelfDetonation extends PillarmanAction {

    public PillarmanSelfDetonation(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.HEAT;
    }
 
    @Override
    public void onHoldTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RED.get(), 12);
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.BOILING_BLOOD_POP.get(), 1);
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if(!world.isClientSide) {
            CustomExplosion.explode(world, user, DamageSource.ON_FIRE.setExplosion(), null, 
                    user.getX(), user.getY(), user.getZ(), 3.0F, 
                    true, Explosion.Mode.BREAK, CustomExplosionType.PILLAR_MAN_DETONATION);
            PlayerEntity playerentity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
            if (playerentity == null || !playerentity.abilities.instabuild) {
                user.hurt(EntityDamageSource.explosion(user), 40F);
                user.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 200, 0));
            }
        }
    }
    
    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
}
