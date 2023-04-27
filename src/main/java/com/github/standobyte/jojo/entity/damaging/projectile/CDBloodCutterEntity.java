package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.BloodParticlesPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
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
        return 4.0F;
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
            Vector3d thisPos = this.getBoundingBox().getCenter();
            IStandPower.getStandPowerOptional(getOwner()).ifPresent(stand -> {
                level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4), 
                        EntityPredicates.ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS).and(
                                entity -> canHaveBloodDropsOn(entity, stand)))
                .forEach(entity -> {
                    Vector3d targetPos = entity.getBoundingBox().getCenter();
                    if (level.clip(new RayTraceContext(thisPos, targetPos, 
                            RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this))
                            .getType() == RayTraceResult.Type.MISS) {
                        stand.getContinuousEffects().getOrCreateEffect(ModStandEffects.DRIED_BLOOD_DROPS.get(), entity).resetTicks();
                        PacketManager.sendToClientsTracking(new BloodParticlesPacket(
                                thisPos, targetPos, 32), this);
                    }
                });
            });
            level.playSound(null, getX(), getY(), getZ(), ModSounds.WATER_SPLASH.get(), getSoundSource(), 1.0F, 1.0F);
        }
        else {
            for (int i = 0; i < 32; i++) {
                ClientUtil.getClientWorld().addParticle(ModParticles.BLOOD.get(), 
                        getX(), getY(), getZ(), 
                        (random.nextDouble() - 0.5) * 0.2, 
                        (random.nextDouble() - 0.5) * 0.2, 
                        (random.nextDouble() - 0.5) * 0.2);
            }
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        if (target instanceof LivingEntity && INonStandPower.getNonStandPowerOptional((LivingEntity) target)
                .map(power -> {
                    if (power.getType() == ModPowers.VAMPIRISM.get()) {
                        target.playSound(ModSounds.VAMPIRE_BLOOD_DRAIN.get(), 1.0F, 1.0F);
                        power.addEnergy(5F);
                        return true;
                    }
                    return false;
                }).orElse(false)) {
            remove();
            return false;
        }
        return super.hurtTarget(target, owner);
    }
    
    public static boolean canHaveBloodDropsOn(Entity target, IStandPower bleedingEntityStand) {
        return !target.is(bleedingEntityStand.getUser()) && target != bleedingEntityStand.getStandManifestation()
                && !(target instanceof StandEntity && !((StandEntity) target).isVisibleForAll())
                && !target.isInWaterOrBubble();
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
