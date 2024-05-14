package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanLightFlash extends PillarmanAction {

    public PillarmanLightFlash(NonStandAction.Builder builder) {
        super(builder);
        mode = Mode.LIGHT;
    }

    @Override
    public void onHoldTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
        	PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RAINBOW.get());
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        int range = 16;
        if (!world.isClientSide()) {
            for (LivingEntity entity : MCUtil.entitiesAround(
                    LivingEntity.class, user, range, false, entity -> 
                    entity.canSee(user) && !(entity instanceof StandEntity && user.is(((StandEntity) entity).getUser())))) {
                if (user.distanceTo(entity) < 5) {
                	entity.addEffect(new EffectInstance(Effects.BLINDNESS, 200, 0));
                } else {
                	entity.addEffect(new EffectInstance(Effects.BLINDNESS, 60, 0));
                }
                if(!(entity instanceof PlayerEntity)) {
                	entity.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), 200, 0));
                }
            }
        }
        user.playSound(ModSounds.AJA_STONE_BEAM.get(), (float) (range + 16) / 16F, 1.0F); // Need to replace
        HamonUtil.createHamonSparkParticlesEmitter(user, 1F, 0F, ModParticles.HAMON_SPARK_RED.get());
        HamonUtil.createHamonSparkParticlesEmitter(user, 1F, 0F, ModParticles.HAMON_SPARK.get());
        HamonUtil.createHamonSparkParticlesEmitter(user, 1F, 0F, ModParticles.HAMON_SPARK_YELLOW.get());
        HamonUtil.createHamonSparkParticlesEmitter(user, 1F, 0F, ModParticles.HAMON_SPARK_BLUE.get());
        HamonUtil.createHamonSparkParticlesEmitter(user, 1F, 0F, ModParticles.HAMON_SPARK_SILVER.get());
        
    }

    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
}
