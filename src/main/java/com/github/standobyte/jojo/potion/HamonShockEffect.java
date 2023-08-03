package com.github.standobyte.jojo.potion;

import java.util.Random;

import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;

public class HamonShockEffect extends StunEffect {

    public HamonShockEffect(int liquidColor) {
        super(liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        
        if (MCUtil.isControlledThisSide(entity)) {
            Random random = entity.getRandom();
            entity.yRot     += (random.nextFloat() - 0.5F) * (amplifier + 1) * 2.5F;
            entity.yHeadRot += (random.nextFloat() - 0.5F) * (amplifier + 1) * 2.5F;
            entity.xRot     += (random.nextFloat() - 0.5F) * (amplifier + 1) * 2.5F;
        }
        
        if (entity.level.isClientSide()) {
            HamonSparksLoopSound.playSparkSound(entity, entity.getBoundingBox().getCenter(), 1.0F, true);
            CustomParticlesHelper.createHamonSparkParticles(entity, entity.getRandomX(0.5), entity.getY(Math.random()), entity.getRandomZ(0.5), 1);
        }
    }
}
