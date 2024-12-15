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
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class PillarmanLightFlash extends PillarmanAction {

    public PillarmanLightFlash(NonStandAction.Builder builder) {
        super(builder);
        mode = Mode.LIGHT;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RAINBOW.get(), 12);
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
                    entity.addEffect(new EffectInstance(Effects.BLINDNESS, 200, 0, true, true, false));
                } else {
                    entity.addEffect(new EffectInstance(Effects.BLINDNESS, 80, 0, true, true, false));
                }
                if (!(entity instanceof PlayerEntity) && !(entity instanceof StandEntity)) {
                    entity.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), 60, 0, true, true, false));
                }
            }
        }
        user.playSound(ModSounds.AJA_STONE_BEAM.get(), (float) (range + 16) / 16F, 1.0F); // TODO replace the light flash sound
        HamonUtil.createHamonSparkParticlesEmitter(user, 2F, 0, ParticleTypes.FLASH);
    }
    
}
