package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGGrapplingStringEntity extends OwnerBoundProjectileEntity {
    private IStandPower userStandPower;
    private boolean bindEntities;
    private StandEntity stand;
    private boolean placedBarrier = false;

    public HGGrapplingStringEntity(World world, StandEntity entity, IStandPower userStand) {
        super(ModEntityTypes.HG_GRAPPLING_STRING.get(), entity, world);
        this.userStandPower = userStand;
    }
    
    public HGGrapplingStringEntity(EntityType<? extends HGGrapplingStringEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!level.isClientSide()) {
            if (userStandPower == null || userStandPower.getHeldAction() != (bindEntities ? ModActions.HIEROPHANT_GREEN_GRAPPLE_ENTITY.get() : ModActions.HIEROPHANT_GREEN_GRAPPLE.get())) {
                remove();
                return;
            }
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
                // KEKW FIXME (!!) free flight
                Vector3d vecToOwner = owner.position().subtract(bound.position());
                if (vecToOwner.lengthSqr() > 4) {
                    dragTarget(bound, vecToOwner.normalize().scale(2));
                    bound.fallDistance = 0;
                }
                if (owner instanceof StandEntity) {
                	// not always works
                    owner.setDeltaMovement(Vector3d.ZERO);
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
                Vector3d grappleVec = vecFromOwner.normalize().scale(2D);
                // FIXME (!!) bumpy movement
//                owner.setDeltaMovement(grappleVec);
                owner.move(MoverType.SELF, grappleVec);
                if (stand == null && owner instanceof StandEntity) {
                    stand = (StandEntity) owner;
                }
                if (stand != null && stand.isFollowingUser()) {
                    LivingEntity user = stand.getUser();
                    if (user != null) {
                        // FIXME (!!) bumpy movement
//                        user.setDeltaMovement(grappleVec);
                        user.move(MoverType.SELF, grappleVec);
                        user.fallDistance = 0;
                    }
                }
            }
            else if (!level.isClientSide() && !placedBarrier && owner instanceof HierophantGreenEntity) {
                HierophantGreenEntity hierophant = (HierophantGreenEntity) owner;
                if (hierophant.hasBarrierAttached()) {
                    hierophant.attachBarrier(blockPosition());
                }
                placedBarrier = true;
            }
            return true;
        }
        return false;
    }
    
    @Override
    protected boolean isBodyPart() {
        return true;
    }

    private static final Vector3d OFFSET = new Vector3d(-0.3, -0.2, 0.55);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    protected int ticksLifespan() {
        return getEntityAttachedTo() == null && !getBlockPosAttachedTo().isPresent() ? 40 : Integer.MAX_VALUE;
    }
    
    @Override
    protected float movementSpeed() {
        return 2.0F;
    }
    
    @Override
    protected boolean canHitEntity(Entity entity) {
        return !entity.is(getOwner());
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (getEntityAttachedTo() == null && bindEntities) {
            if (target instanceof LivingEntity) {
                attachToEntity((LivingEntity) target);
                playSound(ModSounds.HIEROPHANT_GREEN_GRAPPLE_CATCH.get(), 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void updateMotionFlags() {}
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean brokenBlock) {
        if (!brokenBlock && !bindEntities) {
            if (!getBlockPosAttachedTo().isPresent()) {
                playSound(ModSounds.HIEROPHANT_GREEN_GRAPPLE_CATCH.get(), 1.0F, 1.0F);
            }
            attachToBlockPos(blockRayTraceResult.getBlockPos());
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
