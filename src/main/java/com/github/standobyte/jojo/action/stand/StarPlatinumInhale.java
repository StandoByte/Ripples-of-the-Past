package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class StarPlatinumInhale extends StandEntityAction {

    public StarPlatinumInhale(StandEntityAction.Builder builder) {
        super(builder);
    }

    private static final double RANGE = 12;
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        Vector3d mouthPos = standEntity.position()
                .add(0, standEntity.getBbHeight() * 0.75F, 0)
                .add(new Vector3d(0, standEntity.getBbHeight() / 16F, standEntity.getBbWidth() * 0.5F)
                        .xRot(-standEntity.xRot * MathUtil.DEG_TO_RAD).yRot((-standEntity.yRot) * MathUtil.DEG_TO_RAD));
        
        Vector3d spLookVec = standEntity.getLookAngle();
        world.getEntities(standEntity, standEntity.getBoundingBox().inflate(RANGE, RANGE, RANGE), 
                entity -> spLookVec.dot(entity.position().subtract(standEntity.position()).normalize()) > 0.886 && standEntity.canSee(entity)
                && entity.distanceToSqr(standEntity) > 0.5
                && (/*standEntity.isManuallyControlled() || */!entity.is(standEntity.getUser()))).forEach(entity -> {
                    if (entity.canUpdate()) {
                        double distance = entity.distanceTo(standEntity);
                        Vector3d suctionVec = mouthPos.subtract(entity.getBoundingBox().getCenter())
                                .normalize().scale(0.5 * standEntity.getStandEfficiency());
                        entity.setDeltaMovement(distance > 2 ? 
                                entity.getDeltaMovement().add(suctionVec.scale(1 / distance))
                                : suctionVec.scale(Math.max(distance - 1, 0)));
                        if (!world.isClientSide() && distance < 4 && entity instanceof LivingEntity) {
                            DamageUtil.suffocateTick((LivingEntity) entity, 0.025F);
                        }
                    }
                });
        if (world.isClientSide()) {
            GeneralUtil.doFractionTimes(() -> {
                Vector3d particlePos = mouthPos.add(spLookVec.scale(RANGE)
                        .xRot((float) ((Math.random() * 2 - 1) * Math.PI / 6))
                        .yRot((float) ((Math.random() * 2 - 1) * Math.PI / 6)));
                Vector3d vecToStand = mouthPos.subtract(particlePos).normalize().scale(0.75);
                world.addParticle(ModParticles.AIR_STREAM.get(), particlePos.x, particlePos.y, particlePos.z, vecToStand.x, vecToStand.y, vecToStand.z);
            }, 2.5);
        }
    }

    @Override
    public int getCooldownAdditional(IStandPower power, int ticksHeld) {
        int cooldown = super.getCooldownAdditional(power, ticksHeld);
        return cooldownFromHoldDuration(cooldown, power, ticksHeld);
    }

}
