package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStandEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.BloodParticlesPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class CDBloodCutterEntity extends ModdedProjectileEntity {
    
    public CDBloodCutterEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.CD_BLOOD_CUTTER.get(), shooter, world);
    }

    public CDBloodCutterEntity(EntityType<? extends CDBloodCutterEntity> type, World world) {
        super(type, world);
    }

    @Override
    public int ticksLifespan() {
        return 100;
    }

    @Override
    protected float getBaseDamage() {
        return 5.0F;
    }
    
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (targetType != TargetType.ENTITY || ((EntityRayTraceResult) hitTarget).getEntity() instanceof LivingEntity) {
            super.breakProjectile(targetType, hitTarget);
            splashBlood();
        }
    }
    
    private void splashBlood() {
        if (isInWaterOrBubble()) return;
        if (!level.isClientSide()) {
            IStandPower.getStandPowerOptional(getOwner()).ifPresent(stand -> {
                level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4), 
                        EntityPredicates.ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS).and(
                                entity -> !entity.is(stand.getUser()) && entity != stand.getStandManifestation()
                                && !(entity instanceof StandEntity && !((StandEntity) entity).isVisibleForAll())
                                && !entity.isInWaterOrBubble()
                                        // FIXME !! (blood cutter) && isn't behind blocks
                                && entity.getBoundingBox().clip(this.getBoundingBox().getCenter(), entity.getBoundingBox().getCenter()).isPresent()))
                .forEach(entity -> {
                    // FIXME !!! (blood cutter) refresh the timer
                    stand.getContinuousEffects().getOrCreateEffect(ModStandEffects.DRIED_BLOOD_DROPS.get(), entity);

                    PacketManager.sendToClientsTracking(new BloodParticlesPacket(
                            this.getBoundingBox().getCenter(), entity.getBoundingBox().getCenter(), 32), this);
                });
            });
        }
        else {
            level.playLocalSound(getX(), getY(), getZ(), ModSounds.WATER_SPLASH.get(), getSoundSource(), 1.0F, 1.0F, false);
        }
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

}
