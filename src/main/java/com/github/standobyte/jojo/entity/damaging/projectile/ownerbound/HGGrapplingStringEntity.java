package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.UUID;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGGrapplingStringEntity extends OwnerBoundProjectileEntity {
    private static final UUID MANUAL_MOVEMENT_LOCK = UUID.fromString("ccf94bd5-8f0f-4d1e-b606-ba0773d963f3");
    private IStandPower userStandPower;
    private boolean bindEntities;
    private StandEntity stand;
    private boolean placedBarrier = false;
    private boolean caughtAnEntity = false;

    public HGGrapplingStringEntity(World world, StandEntity entity, IStandPower userStand) {
        super(ModEntityTypes.HG_GRAPPLING_STRING.get(), entity, world);
        this.stand = entity;
        this.userStandPower = userStand;
    }
    
    public HGGrapplingStringEntity(EntityType<? extends HGGrapplingStringEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level.isClientSide() && stand != null && caughtAnEntity) {
            stand.getManualMovementLocks().removeLock(MANUAL_MOVEMENT_LOCK);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!level.isClientSide() && (userStandPower == null || userStandPower.getHeldAction() != (
                bindEntities ? ModStandsInit.HIEROPHANT_GREEN_GRAPPLE_ENTITY.get() : 
                    ModStandsInit.HIEROPHANT_GREEN_GRAPPLE.get()))) {
            remove();
            return;
        }
        LivingEntity bound = getEntityAttachedTo();
        if (bound != null) {
            LivingEntity owner = getOwner();
            if (!bound.isAlive()) {
                if (!level.isClientSide()) {
                    remove();
                }
            }
            else {
                Vector3d vecToOwner = owner.position().subtract(bound.position());
                double length = vecToOwner.length();
                if (length < 2) {
                    if (!level.isClientSide()) {
                        remove();
                    }
                }
                else {
                    dragTarget(bound, vecToOwner.normalize().scale(1));
                    bound.fallDistance = 0;
                }
            }
        }
    }
    
    public void setBindEntities(boolean bindEntities) {
        this.bindEntities = bindEntities;
    }
    
    @Override
    protected boolean moveToBlockAttached() {   
        if (super.moveToBlockAttached()) {
            LivingEntity owner = getOwner();
            Vector3d vecFromOwner = position().subtract(owner.position());
            if (vecFromOwner.lengthSqr() > 4) {
                Vector3d grappleVec = vecFromOwner.normalize().scale(2);
                Entity entity = owner;
                if (stand == null && owner instanceof StandEntity) {
                    stand = (StandEntity) owner;
                }
                if (stand != null && stand.isFollowingUser()) {
                    LivingEntity user = stand.getUser();
                    if (user != null) {
                        entity = user;
                    }
                }
                entity = entity.getRootVehicle();
                entity.setDeltaMovement(grappleVec);
                entity.fallDistance = 0;
            }
            else if (!level.isClientSide()) {
                remove();
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }

    private static final Vector3d OFFSET = new Vector3d(-0.3, -0.2, 0.55);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    public int ticksLifespan() {
        return getEntityAttachedTo() == null && !getBlockPosAttachedTo().isPresent() ? 40 : Integer.MAX_VALUE;
    }
    
    @Override
    protected float movementSpeed() {
        return 4F;
    }
    
    @Override
    protected boolean canHitEntity(Entity entity) {
        LivingEntity owner = getOwner();
        if (entity.is(owner) || !(entity instanceof LivingEntity)) {
            return false;
        }
        if (owner instanceof StandEntity) {
            StandEntity stand = (StandEntity) getOwner();
            return !entity.is(stand.getUser()) || !stand.isFollowingUser();
        }
        return true;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (getEntityAttachedTo() == null && bindEntities) {
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (!JojoModUtil.isTargetBlocking(livingTarget)) {
                    attachToEntity(livingTarget);
                    playSound(ModSounds.HIEROPHANT_GREEN_GRAPPLE_CATCH.get(), 1.0F, 1.0F);
                    caughtAnEntity = true;
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    protected void updateMotionFlags() {}
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean brokenBlock) {
        BlockPos blockHitPos = blockRayTraceResult.getBlockPos();
        BlockState hitBlock = level.getBlockState(blockHitPos);
        if (hitBlock.getBlock() == Blocks.BARRIER) {
            remove();
            return;
        }
        
        if (!brokenBlock && !bindEntities) {
            if (!getBlockPosAttachedTo().isPresent()) {
                playSound(ModSounds.HIEROPHANT_GREEN_GRAPPLE_CATCH.get(), 1.0F, 1.0F);
                attachToBlockPos(blockHitPos);
            }
            
            placeBarrier(blockHitPos);
        }
    }
    
    private void placeBarrier(BlockPos blockPos) {
        if (!level.isClientSide() && !placedBarrier && getOwner() instanceof HierophantGreenEntity) {
            HierophantGreenEntity hierophant = (HierophantGreenEntity) getOwner();
            if (hierophant.hasBarrierAttached()) {
                hierophant.attachBarrier(blockPos);
            }
            placedBarrier = true;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public float getBaseDamage() {
        return 0;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return true;
    }
}
