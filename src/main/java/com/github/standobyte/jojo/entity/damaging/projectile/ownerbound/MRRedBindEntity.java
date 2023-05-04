package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MRRedBindEntity extends OwnerBoundProjectileEntity {
    protected static final DataParameter<Boolean> KICK_COMBO = EntityDataManager.defineId(MRRedBindEntity.class, DataSerializers.BOOLEAN);
    
    private StandEntity ownerStand;
    private EffectInstance immobilizedEffect = null;
    private int ticksTargetClose = 0;

    public MRRedBindEntity(World world, StandEntity entity) {
        super(ModEntityTypes.MR_RED_BIND.get(), entity, world);
        this.ownerStand = entity;
    }
    
    public MRRedBindEntity(EntityType<? extends MRRedBindEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
            return;
        }
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!level.isClientSide()) {
            if (ownerStand == null || ownerStand.getCurrentTaskAction() != ModStandsInit.MAGICIANS_RED_RED_BIND.get() && !isInKickCombo()) {
                remove();
                return;
            }
        }
        LivingEntity bound = getEntityAttachedTo();
        if (bound != null) {
            LivingEntity owner = getOwner();
            if (!bound.isAlive() || owner.distanceToSqr(bound) > 100) {
                if (!level.isClientSide()) {
                    remove();
                }
            }
            else {
                if (!level.isClientSide()) {
                    if (bound.getRemainingFireTicks() % 20 == 0 || bound.getRemainingFireTicks() <= 0) {
                        DamageUtil.setOnFire(bound, 3, true);
                    }
                    if (isInKickCombo()) {
                        DamageUtil.suffocateTick(bound, 1);
                    }
                }
                Vector3d vecToOwner = owner.position().subtract(bound.position());
                if (vecToOwner.lengthSqr() > 4) {
                    dragTarget(bound, vecToOwner.normalize().scale(0.2));
                    ticksTargetClose = 0;
                }
                else if (!level.isClientSide() && !isInKickCombo() && ticksTargetClose++ > 10) {
                    remove();
                }
            }
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerWorld) level);
        }
    }
    
    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean isFiery() {
        return true;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (getEntityAttachedTo() == null) {
            if (target instanceof LivingEntity && !target.isInvulnerableTo(getDamageSource(owner))) {
                LivingEntity targetLiving = (LivingEntity) target;
                attachToEntity(targetLiving);
                if (!level.isClientSide()) {
                    boolean thisEffect = immobilizedEffect == targetLiving.getEffect(ModEffects.IMMOBILIZE.get());
                    targetLiving.addEffect(new EffectInstance(ModEffects.IMMOBILIZE.get(), ticksLifespan() - tickCount));
                    if (thisEffect) {
                        immobilizedEffect = targetLiving.getEffect(ModEffects.IMMOBILIZE.get());
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected double attachedTargetHeight() {
        return this.isInKickCombo() ? 0.75 : super.attachedTargetHeight();
    }
    
    @Override
    public void remove() {
        if (!level.isClientSide() && immobilizedEffect != null) {
            LivingEntity bound = getEntityAttachedTo();
            if (bound != null) {
                MCUtil.removeEffectInstance(bound, immobilizedEffect);
            }
        }
        super.remove();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(KICK_COMBO, false);
    }
    
    public void setKickCombo() {
        entityData.set(KICK_COMBO, true);
        LivingEntity target = getEntityAttachedTo();
        if (target != null) {
            MCUtil.removeEffectInstance(target, immobilizedEffect);
            target.addEffect(new EffectInstance(ModEffects.STUN.get(), ticksLifespan() - tickCount));
            immobilizedEffect = target.getEffect(ModEffects.STUN.get());
        }
    }
    
    public boolean isInKickCombo() {
        return entityData.get(KICK_COMBO);
    }
    
    public StandEntity getOwnerAsStand() {
        if (ownerStand == null) {
            LivingEntity owner = getOwner();
            ownerStand = owner instanceof StandEntity ? (StandEntity) owner : null;
        }
        return ownerStand;
    }

    private static final Vector3d OFFSET = new Vector3d(0, -0.25, 0.5);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return 0.0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    public int ticksLifespan() {
        return isAttachedToAnEntity() ? 
                isInKickCombo() ? Integer.MAX_VALUE : 100
                : 7;
    }
    
    @Override
    protected float movementSpeed() {
        return 1.0F;
    }
    
    @Override
    protected void updateMotionFlags() {}
}
