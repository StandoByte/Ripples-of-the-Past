package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStandTypes;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SCRapierEntity extends ModdedProjectileEntity {
    private static final int MAX_RICOCHETS = 12;
    private int ricochetCount;
    
    public SCRapierEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.SC_RAPIER.get(), shooter, world);
    }

    public SCRapierEntity(EntityType<? extends SCRapierEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    protected float getBaseDamage() {
        LivingEntity owner = getOwner();
        float damage;
        if (owner != null) {
            damage = (float) owner.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        }
        else {
            damage = (float) ModStandTypes.SILVER_CHARIOT.get().getStats().getBasePower();
        }
        return damage * 1.5F;
    }
    
    @Override
    protected float getDamageFinalCalc(float damage) {
        return damage + (float) ricochetCount * 1F;
    }
    
    @Override
    protected boolean debuffsFromStand() {
    	return false;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof SkeletonEntity && random.nextFloat() < 0.05F);
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
	public int ticksLifespan() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void breakProjectile(TargetType targetType) {
    }
    
    @Override
    protected void onHitBlock(BlockRayTraceResult blockRayTraceResult) {
        boolean ricochet = false;
        if (ricochetCount < MAX_RICOCHETS) {
            BlockPos blockPos = blockRayTraceResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            SoundType soundType = blockState.getSoundType(level, blockPos, this);
            level.playSound(null, blockPos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
            Direction hitFace = blockRayTraceResult.getDirection();
            ricochet = ricochet(hitFace);
        }
        if (!ricochet) {
            Vector3d pos = position();
            Vector3d movementVec = getDeltaMovement();
            Direction hitFace = blockRayTraceResult.getDirection();
            Vector3d blockVec = Vector3d.atCenterOf(blockRayTraceResult.getBlockPos()).add(Vector3d.atLowerCornerOf(hitFace.getNormal()).scale(0.5));
            double k;
            switch (hitFace.getAxis()) {
            case X:
                k = (blockVec.x - pos.x) / movementVec.x;
                break;
            case Y:
                k = (blockVec.y - pos.y) / movementVec.y;
                break;
            case Z:
                k = (blockVec.z - pos.z) / movementVec.z;
                break;
            default:
                return;
            }
            setPos(
                    getX() + movementVec.x * k, 
                    getY() + movementVec.y * k, 
                    getZ() + movementVec.z * k);
            setDeltaMovement(Vector3d.ZERO);
        }
    }
    
    private boolean ricochet(Direction hitSurfaceDirection) {
        if (hitSurfaceDirection != null) {
            Vector3d motion = getDeltaMovement();
            Vector3d motionNew;
            switch (hitSurfaceDirection.getAxis()) {
            case X:
                motionNew = new Vector3d(-motion.x, motion.y, motion.z);
                break;
            case Y:
                motionNew = new Vector3d(motion.x, -motion.y, motion.z);
                break;
            case Z:
                motionNew = new Vector3d(motion.x, motion.y, -motion.z);
                break;
            default:
                return false;
            }
            Vector3d clipStart = position();
            Vector3d clipEnd = clipStart.add(motionNew.normalize().scale(16));
            if (level.clip(new RayTraceContext(clipStart, clipEnd, 
                    RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, this)).getType() == RayTraceResult.Type.MISS) {
                LivingEntity owner = getOwner();
                if (owner == null) {
                	return false;
                }
                if (!owner.getBoundingBox().clip(clipStart, clipEnd).isPresent()) {
                	if (owner instanceof StandEntity) {
                		LivingEntity user = ((StandEntity) owner).getUser();
                		if (user == null || !user.getBoundingBox().clip(clipStart, clipEnd).isPresent()) {
                			return false;
                		}
                	}
                	else {
                		return false;
                	}
                }
            }
            setDeltaMovement(motionNew);
            rotateTowardsMovement(1.0F);
            ricochetCount++;
            return true;
        }
        return false;
    }

    @Override
    public void playerTouch(PlayerEntity player) {
        if (!level.isClientSide()) {
            if (getOwner() instanceof SilverChariotEntity) {
                SilverChariotEntity stand = (SilverChariotEntity) getOwner();
                if (stand.isFollowingUser() && player.is(stand.getUser())) {
                    takeRapier(stand);
                }
            }
        }
    }
    
    public void takeRapier(SilverChariotEntity stand) {
        if (stand.is(getOwner())) {
            stand.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
            stand.setRapier(true);
            remove();
        }
    }
    
    
    private static final Vector3d OFFSET_YROT = new Vector3d(0.0, -0.29, 0.375);
    private static final Vector3d OFFSET_XROT = new Vector3d(0, 0.0, 1.375);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET_YROT;
    }
    
    @Override
    protected Vector3d getXRotOffset() {
        return OFFSET_XROT;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Ricochets", ricochetCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        ricochetCount = nbt.getInt("Ricochets");
    }

}
