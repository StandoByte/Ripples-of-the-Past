package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class ItemProjectileEntity extends AbstractArrowEntity implements IEntityAdditionalSpawnData {
    protected boolean leftOwner;

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> type, LivingEntity thrower, World world) {
        super(type, thrower, world);
        if (thrower instanceof PlayerEntity && ((PlayerEntity) thrower).abilities.instabuild) {
            pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
        }
    }

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> type, double x, double y, double z, World world) {
        super(type, x, y, z, world);
    }

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> type, World world) {
        super(type, world);
    }

    public void shootFromRotation(Entity shooter, float velocity, float inaccuracy) {
        shootFromRotation(shooter, shooter.xRot, shooter.yRot, 0, velocity, inaccuracy);
    }

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        if (rayTraceResult.getType() == Type.BLOCK) {
            BlockPos blockPos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            setSoundEvent(getActualHitGroundSound(blockState, blockPos));
            super.onHit(rayTraceResult);
            shakeTime = 0;
            setNoGravity(false);
            setSoundEvent(getDefaultHitGroundSoundEvent());
        }
        else {
            super.onHit(rayTraceResult);
        }
    }

    protected SoundEvent getActualHitGroundSound(BlockState blockState, BlockPos blockPos) {
        return blockState.getBlock().getSoundType(blockState, level, blockPos, this).getBreakSound();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (super.canHitEntity(entity)) {
            if (!canHitOwnerProjectile() && entity instanceof ProjectileEntity) {
                Entity ownerThis = getOwner();
                Entity ownerThat = ((ProjectileEntity) entity).getOwner();
                return ownerThis == null || ownerThat == null || ownerThis.getUUID() != ownerThat.getUUID();
            }
            return true;
        }
        return false;
    }
    
    protected boolean canHitOwnerProjectile() {
        return false;
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult) {
        Entity target = entityRayTraceResult.getEntity();
        Entity thrower = getOwner();
        if (thrower instanceof LivingEntity) {
            ((LivingEntity) thrower).setLastHurtMob(target);
        }
        boolean dodge = target.getType() == EntityType.ENDERMAN;
        int prevTargetFireTimer = target.getRemainingFireTicks();
        if (isOnFire() && !dodge) {
            target.setSecondsOnFire(5);
        }
        if (hurtTarget(target, thrower)) {
            if (dodge) {
                return;
            }
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity)target;
                if (!level.isClientSide() && thrower instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingTarget, thrower);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) thrower, livingTarget);
                }
                doPostHurtEffects(livingTarget);
            }
            playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            if (isRemovedOnEntityHit()) {
                remove();
            }
            else {
                changeMovementAfterHit();
            }
        } else {
            target.setRemainingFireTicks(prevTargetFireTimer);
            setDeltaMovement(getDeltaMovement().scale(-0.1D));
            yRot += 180.0F;
            yRotO += 180.0F;
            if (!level.isClientSide() && getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (isRemovedOnEntityHit()) {
                    if (pickup == AbstractArrowEntity.PickupStatus.ALLOWED) {
                        spawnAtLocation(getPickupItem(), 0.1F);
                    }
                    remove();
                }
                else {
                    changeMovementAfterHit();
                }
            }
        }
    }

    protected boolean hurtTarget(Entity target, Entity thrower) {
        float dmgAmount = getActualDamage();
        DamageSource damagesource = DamageSource.arrow(this, thrower == null ? this : thrower);
        return target.hurt(damagesource, (float) dmgAmount);
    }

    @Override
    public void playerTouch(PlayerEntity player) {
        if (!level.isClientSide()) {
            Entity shooter = getOwner();
            if (inGround || shooter == null || shooter.getUUID() == player.getUUID()) {
                boolean canPickUp = (pickup == AbstractArrowEntity.PickupStatus.ALLOWED 
                        || pickup == AbstractArrowEntity.PickupStatus.CREATIVE_ONLY && player.abilities.instabuild)
                        && (inGround || isNoPhysics() || throwerCanCatch());
                if (canPickUp && pickup == AbstractArrowEntity.PickupStatus.ALLOWED && !player.inventory.add(getPickupItem())) {
                    canPickUp = false;
                }
                if (canPickUp) {
                    pickUp(player);
                    return;
                }
            }
            super.playerTouch(player);
        }
    }
    
    protected void pickUp(PlayerEntity player) {
        player.take(this, 1);
        remove();
    }

    public final boolean isInGround() {
        return inGround;
    }

    protected boolean throwerCanCatch() {
        if (!leftOwner) {
            leftOwner = CommonReflection.getProjectileLeftOwner(this);
        }
        return leftOwner;
    }

    protected float getActualDamage() {
        float dmgAmount = (float) (getDeltaMovement().length() * getBaseDamage());
        if (isCritArrow()) {
            dmgAmount += random.nextInt((int) (dmgAmount / 2) + 2);
        }
        return dmgAmount;
    }

    protected boolean isRemovedOnEntityHit() {
        return true;
    }

    protected void changeMovementAfterHit() {
        setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        tickCount = compound.getInt("Age");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Age", tickCount);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(getOwner() != null ? getOwner().getId() : -1);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        int ownerId = additionalData.readInt();
        if (ownerId > -1) {
            setOwner(level.getEntity(ownerId));
        }
    }
}