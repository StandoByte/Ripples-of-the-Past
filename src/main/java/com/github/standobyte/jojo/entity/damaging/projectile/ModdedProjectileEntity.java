package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class ModdedProjectileEntity extends DamagingEntity {
    private static final DataParameter<Boolean> IS_DEFLECTED = EntityDataManager.defineId(ModdedProjectileEntity.class, DataSerializers.BOOLEAN);
    private int ownerId = -1;
    
    protected ModdedProjectileEntity(EntityType<? extends ModdedProjectileEntity> type, LivingEntity shooter, World world) {
        super(type, shooter, world);
        if (!hasGravity()) {
            setNoGravity(true);
        }
    }

    public ModdedProjectileEntity(EntityType<? extends ModdedProjectileEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void setOwner(Entity owner) {
        super.setOwner(owner);
        this.ownerId = owner == null ? -1 : owner.getId();
    }

    @Override
    public LivingEntity getOwner() {
        LivingEntity owner = super.getOwner();
        if (owner == null) {
            setOwner(level.getEntity(ownerId));
            owner = super.getOwner();
        }
        return owner;
    }
    
    public void shootFromRotation(Entity shooter, float velocity, float inaccuracy) {
        shootFromRotation(shooter, shooter.xRot, shooter.yRot, 0, velocity, inaccuracy);
    }

    @Override
    public void shootFromRotation(Entity shooter, float xRot, float yRot, float yAxisRotOffset, float velocity, float inaccuracy) {
        Vector3d shootingVec = Vector3d.directionFromRotation(xRot, yRot);
        shoot(shootingVec.x, shootingVec.y, shootingVec.z, velocity, inaccuracy);
        addShooterMotion(shooter);
    }
    
    protected void addShooterMotion(Entity shooter) {
        if (isNoGravity()) {
            Vector3d shooterMotion = shooter.getDeltaMovement();
            setDeltaMovement(getDeltaMovement().add(shooterMotion.x, shooter.isOnGround() ? 0.0D : shooterMotion.y, shooterMotion.z));
        }
    }
    
    public void shoot(Entity shooter, Entity target, float velocity, float inaccuracy) {
        shoot(target.getX(), target.getY(), target.getZ(), velocity, inaccuracy);
        addShooterMotion(shooter);
    }
    
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        Vector3d movement = getDeltaMovement();
        yRot = MathUtil.yRotDegFromVec(movement);
        xRot = MathUtil.xRotDegFromVec(movement);
        yRotO = yRot;
        xRotO = xRot;
    }

    @Override
    public void tick() {
        Entity owner = getOwner();
        if (!level.isClientSide() && (tickCount > ticksLifespan() || owner != null && !owner.isAlive())) {
            remove();
            return;
        }
        super.tick();
        moveProjectile();
    }
    
    protected void moveProjectile() {
        Vector3d movementVec = getDeltaMovement();
        double x = getX();
        double y = getY();
        double z = getZ();
        double nextX = x + movementVec.x;
        double nextY = y + movementVec.y;
        double nextZ = z + movementVec.z;
        
        if (!constVelocity()) {
            if (!isNoGravity()) {
                setDeltaMovement(movementVec.x, movementVec.y - getGravityAcceleration(), movementVec.z);
            }   
            
            rotateTowardsMovement(1.0F);
            
            double inertia = getInertia();
            if (isInWater()) {
                double bubblePosFactor = 0.25D;
                for (int j = 0; j < 4; ++j) {
                    level.addParticle(ParticleTypes.BUBBLE, 
                            nextX - movementVec.x * bubblePosFactor, 
                            nextY - movementVec.y * bubblePosFactor, 
                            nextZ - movementVec.z * bubblePosFactor, 
                            movementVec.x, movementVec.y, movementVec.z);
                }
                inertia *= getWaterInertiaFactor();
            }
            setDeltaMovement(movementVec.scale(inertia));
        }
        
        xo = x;
        yo = y;
        zo = z;
        xOld = x;
        yOld = y;
        zOld = z;
        setPos(nextX, nextY, nextZ);
        
//        IParticleData particle = getParticle();
//        if (particle != null) {
//            level.addParticle(particle, nextX, nextY, nextZ, 0.0D, 0.0D, 0.0D);
//        }
//        particle = getTrailParticle();
//        if (particle != null) {
//            for (int i = 0; i < 4; ++i) {
//                level.addParticle(getParticle(), x + movementVec.x * i / 4.0D, y + movementVec.y * i / 4.0D, z + movementVec.z * i / 4.0D, -movementVec.x, -movementVec.y + 0.2D, -movementVec.z);
//            }
//        }
    }
    
    protected boolean constVelocity() {
        return true;
    }
    
    protected boolean hasGravity() {
        return false;
    }

    protected double getGravityAcceleration() {
        return 0.03D;
    }
    
    protected double getInertia() {
        return 0.99D;
    }
    
    protected double getWaterInertiaFactor() {
        return 0.8D;
    }
    
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (!level.isClientSide()) {
            remove();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (super.canHitEntity(entity)) {
            LivingEntity owner = getOwner();
            if (owner == null) {
                return true;
            }
            if (entity instanceof ProjectileEntity) {
                Entity otherOwner = ((ProjectileEntity) entity).getOwner();
                return otherOwner == null || owner.getUUID() != otherOwner.getUUID();
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean canHitOwner() {
        return entityData.get(IS_DEFLECTED);
    }
    
    public void setIsDeflected() {
        entityData.set(IS_DEFLECTED, true);
    }
    
    @Override
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        breakProjectile(TargetType.ENTITY, entityRayTraceResult);
    }
    
    @Override
    protected void onHitBlock(BlockRayTraceResult blockRayTraceResult) {
        super.onHitBlock(blockRayTraceResult);
        breakProjectile(TargetType.BLOCK, blockRayTraceResult);
    }
    
    protected void rotateTowardsMovement(float rotationSpeed) {
        Vector3d motionVec = getDeltaMovement();
        if (motionVec.lengthSqr() != 0) {
            yRot = MathUtil.yRotDegFromVec(motionVec);
            xRot = MathUtil.xRotDegFromVec(motionVec);
            while(xRot - xRotO < -180.0F) {
                xRotO -= 360.0F;
            }
            while(xRot - xRotO >= 180.0F) {
                xRotO += 360.0F;
            }
            while(yRot - yRotO < -180.0F) {
                yRotO -= 360.0F;
            }
            while(yRot - yRotO >= 180.0F) {
                yRotO += 360.0F;
            }
            yRot = MathHelper.lerp(rotationSpeed, yRotO, yRot);
            xRot = MathHelper.lerp(rotationSpeed, xRotO, xRot);
        }
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        markHurt();
        breakProjectile(TargetType.EMPTY, null);
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }
    
//    @Nullable
//    protected IParticleData getParticle() {
//        return null;
//    }
//    
//    @Nullable
//    protected IParticleData getTrailParticle() {
//        return null;
//    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = getBoundingBox().getSize() * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }
        d0 = d0 * 64.0D * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_DEFLECTED, false);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        if (ownerId < 0) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerId = owner.getId();
            }
        }
        buffer.writeInt(ownerId);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.ownerId = additionalData.readInt();
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("IsDeflected", entityData.get(IS_DEFLECTED));
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        entityData.set(IS_DEFLECTED, nbt.getBoolean("IsDeflected"));
    }
}
