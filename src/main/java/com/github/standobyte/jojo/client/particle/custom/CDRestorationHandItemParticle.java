package com.github.standobyte.jojo.client.particle.custom;

import com.github.standobyte.jojo.client.particle.CDRestorationParticle;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

public class CDRestorationHandItemParticle extends EntityPosParticle {
    private final LivingEntity livingEntity;
    private final Vector3d randomOffset;
    private final Hand hand;

    private CDRestorationHandItemParticle(ClientWorld world, LivingEntity entity, Hand hand) {
        super(world, entity, true);
        this.livingEntity = entity;
        this.hand = hand;
        this.randomOffset = new Vector3d((random.nextDouble() - 0.5), (random.nextDouble() - 0.5), (random.nextDouble() - 0.5)).scale(0.5);
        initPos();
    }
    
    @Override
    protected Vector3d getNextTickPos(Vector3d entityPos) {
        return livingEntity != null ? entityPos.add(randomOffset).add(new Vector3d(
                livingEntity.getBbWidth() * 0.6 * (livingEntity.getMainArm() == (hand == Hand.MAIN_HAND ? HandSide.RIGHT : HandSide.LEFT) ? -1 : 1), 
                livingEntity.getBbHeight() * (entity.isShiftKeyDown() ? 0.25 : 0.45), 
                livingEntity.getBbWidth() * 0.7)
                .yRot(-livingEntity.yBodyRot * MathUtil.DEG_TO_RAD)) : null;
    }
    
    public static EntityPosParticle createCustomParticle(ClientWorld world, LivingEntity entity, Hand hand) {
        EntityPosParticle particle = new CDRestorationHandItemParticle(world, entity, hand);
        particle.pickSprite(CDRestorationParticle.Factory.getSprite());
        particle.setLifetime(5);
        return particle;
    }

}
