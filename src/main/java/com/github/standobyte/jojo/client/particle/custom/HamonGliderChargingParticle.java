package com.github.standobyte.jojo.client.particle.custom;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

public class HamonGliderChargingParticle extends EntityPosParticle {
    private final LivingEntity livingEntity;
    private final Vector3d randomOffset;
    private final Hand hand;

    private HamonGliderChargingParticle(ClientWorld world, LivingEntity entity, Hand hand) {
        super(world, entity, false);
        this.livingEntity = entity;
        this.hand = hand;
        this.randomOffset = new Vector3d(
                (random.nextDouble() - 0.5), 
                random.nextDouble() * 0.5, 
                (random.nextDouble() - 0.5)).scale(0.5);
        setLifetime(3);
        initPos();
    }
    
    @Override
    public void tick() {
        if (livingEntity == null || !livingEntity.isAlive()
                || livingEntity.getVehicle() == null
                || livingEntity.getVehicle().getType() != ModEntityTypes.LEAVES_GLIDER.get()) {
            remove();
            return;
        }
        super.tick();
    }
    
    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }
    
    @Override
    protected Vector3d getNextTickPos(Vector3d entityPos) {
        return livingEntity != null ? entityPos.add(randomOffset).add(new Vector3d(
                livingEntity.getBbWidth() * 0.6 * (livingEntity.getMainArm() == (hand == Hand.MAIN_HAND ? HandSide.RIGHT : HandSide.LEFT) ? -1 : 1), 
                livingEntity.getBbHeight(), 
                0)
                .yRot(-livingEntity.yBodyRot * MathUtil.DEG_TO_RAD)) : null;
    }
    
    public static EntityPosParticle createCustomParticle(ClientWorld world, LivingEntity entity, Hand hand) {
        EntityPosParticle particle = new HamonGliderChargingParticle(world, entity, hand);
        particle.pickSprite(CustomParticlesHelper.getSavedSpriteSet(ModParticles.HAMON_SPARK.get()));
        particle.setLifetime(5);
        return particle;
    }

}
