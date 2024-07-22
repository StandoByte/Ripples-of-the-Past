package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.potion.ImmobilizeEffect;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Effect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class OwnerBoundProjectileEntity extends ModdedProjectileEntity {
    protected static final DataParameter<Boolean> IS_BOUND_TO_OWNER = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> BLOCK_ATTACHED_TO = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Integer> ENTITY_ATTACHED_TO = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.INT);
    private static final DataParameter<Boolean> IS_MOVING_FORWARD = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> IS_RETRACTING = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.BOOLEAN);
    private double distance;
    private LivingEntity attachedEntity;
    private UUID attachedEntityUUID;
    private int lifeSpan;

    public OwnerBoundProjectileEntity(EntityType<? extends OwnerBoundProjectileEntity> entityType, @Nonnull LivingEntity owner, World world) {
        super(entityType, owner, world);
    }

    public OwnerBoundProjectileEntity(EntityType<? extends OwnerBoundProjectileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        if (isBoundToOwner()) {
            LivingEntity owner = getOwner();
            if (owner == null) {
                if (!level.isClientSide()) {
                    remove();
                }
                return;
            }
        }
        if (!level.isClientSide() && attachedEntityUUID != null && attachedEntity == null) {
            Entity entity = ((ServerWorld) level).getEntity(attachedEntityUUID);
            if (entity instanceof LivingEntity) {
                attachToEntity((LivingEntity) entity);
                attachedEntityUUID = null;
            }
        }
        dragged.forEach(entity -> entity.setDeltaMovement(Vector3d.ZERO));
        dragged.clear();
        super.tick();
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        dragged.forEach(entity -> entity.setDeltaMovement(Vector3d.ZERO));
    }
    
    @Override
    protected void moveProjectile() {
        if (!moveToEntityAttached() && !moveToBlockAttached() && !moveBoundToOwner()) {
            super.moveProjectile();
        }
    }
    
    protected boolean moveBoundToOwner() {
        if (isBoundToOwner()) {
            Entity owner = getOwner();
            setRot(owner.yRot, owner.xRot);
    
            Vector3d originPoint = ownerPosition(1.0F, false);
            Vector3d nextOriginOffset = getNextOriginOffset();
            if (nextOriginOffset == null) {
                if (!level.isClientSide()) {
                    remove();
                }
                return true;
            }
    
            double x = getX();
            double y = getY();
            double z = getZ();
            double nextX = originPoint.x + nextOriginOffset.x;
            double nextY = originPoint.y + nextOriginOffset.y;
            double nextZ = originPoint.z + nextOriginOffset.z;
            if (!level.getChunkSource().hasChunk(MathHelper.floor(nextX) >> 4, MathHelper.floor(nextZ) >> 4)) {
                if (level.isClientSide()) {
                    remove();
                }
                return false;
            }
            setDeltaMovement(new Vector3d(nextX - getX(), nextY - getY(), nextZ - getZ()));
    
            xo = x;
            yo = y;
            zo = z;
            xOld = x;
            yOld = y;
            zOld = z;
            setPos(nextX, nextY, nextZ);
            return true;
        }
        return false;
    }
    
    protected boolean moveToEntityAttached() {
        LivingEntity bound = getEntityAttachedTo();
        if (bound != null) {
            moveTo(bound.getX(), bound.getY(attachedTargetHeight()), bound.getZ(), bound.yRot, bound.xRot);
            return true;
        }
        return false;
    }
    
    protected double attachedTargetHeight() {
        return 0.5;
    }
    
    protected boolean moveToBlockAttached() {
        Optional<BlockPos> blockPosOptional = getBlockPosAttachedTo();
        if (blockPosOptional.isPresent()) {
            BlockPos blockPos = blockPosOptional.get();
            moveTo(
                    blockPos.getX() + 0.5D, 
                    blockPos.getY() + 0.5D, 
                    blockPos.getZ() + 0.5D);
            return true;
        }
        return false;
    }
    
    protected final Vector3d getOriginPoint() {
        return getOriginPoint(1.0F);
    }
    
    public Vector3d getOriginPoint(float partialTick) {
        return ownerPosition(partialTick, isBodyPart());
    }
    
    protected final Vector3d ownerPosition(float partialTick, boolean useBodyRotation) {
        LivingEntity owner = getOwner();
        if (owner != null) {
            return getPos(owner, partialTick, 
                    useBodyRotation ? MathHelper.lerp(partialTick, owner.yBodyRotO, owner.yBodyRot) : MathHelper.lerp(partialTick, owner.yRotO, owner.yRot), 
                            MathHelper.lerp(partialTick, owner.xRotO, owner.xRot));
        }
        return MCUtil.getEntityPosition(this, partialTick);
    }
    
    public boolean isBodyPart() {
        return false;
    }
    
    @Nullable
    protected Vector3d getNextOriginOffset() {
        LivingEntity owner = getOwner();
        double distance = updateDistance();
        updateMotionFlags();
        if (isRetracting() && distance <= 0) {
            return null;
        }
        setDistance(distance);
        return originOffset(owner.yRot, owner.xRot, distance);
    }
    
    protected float updateDistance() {
        if (isRetracting()) {
            return (float) (getDistance() - retractSpeed() * speedFactor);
        }
        if (isMovingForward()) {
            return (float) (getDistance() + movementSpeed() * speedFactor);
        }
        return (float) getDistance();
    }
    
    protected abstract float movementSpeed();
    
    protected int timeAtFullLength() {
        return 0;
    }

    protected float retractSpeed() {
        return movementSpeed();
    }
    
    protected void updateMotionFlags() {
        int stopForwardMotionMark = (int) (maxDistance() / movementSpeed());
        if (isMovingForward() && tickCount >= stopForwardMotionMark) {
            setIsMovingForward(false);
        }
        if (!isRetracting() && tickCount >= stopForwardMotionMark + timeAtFullLength()) {
            setIsRetracting(true);
        }
    }
    
    private double maxDistance() {
        return movementSpeed() * retractSpeed() * (ticksLifespan() - timeAtFullLength()) / (movementSpeed() + retractSpeed());
    }
    
    protected Vector3d originOffset(float yRot, float xRot, double distance) {
        return Vector3d.directionFromRotation(xRot, yRot).scale(distance);
    }
    
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return getBoundingBox().expandTowards(getOriginPoint().subtract(position()));
    }

    @Override
    protected RayTraceResult[] rayTrace() {
        Vector3d startPos = getOriginPoint();
        Vector3d endPos = position().add(getDeltaMovement());
        Vector3d rtVec = startPos.subtract(endPos);
        AxisAlignedBB aabb = getBoundingBox().expandTowards(rtVec).inflate(1.0D);
        double minDistance = rtVec.length();
        return JojoModUtil.rayTraceMultipleEntities(startPos, endPos, aabb, minDistance, level, this, this::canHitEntity, getBbWidth() / 2, 0);
    }
    
    @Override
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float dmgAmount) {
        return shouldHurtThroughInvulTicks() ? super.hurtTarget(target, dmgSource, dmgAmount) : 
            target.hurt(DamageUtil.enderDragonDamageHack(dmgSource, target), dmgAmount);
    }
    
    protected boolean shouldHurtThroughInvulTicks() {
        return false;
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {}

    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {
        if (!blockDestroyed) {
            setIsRetracting(true);
        }
    }
    
    protected void setBoundToOwner(boolean value) {
        entityData.set(IS_BOUND_TO_OWNER, value);
    }
    
    public boolean isBoundToOwner() {
        return entityData.get(IS_BOUND_TO_OWNER);
    }
    
    public void attachToEntity(LivingEntity boundTarget) {
        this.attachedEntity = boundTarget;
        entityData.set(ENTITY_ATTACHED_TO, boundTarget.getId());
    }
    
    @Nullable
    public LivingEntity getEntityAttachedTo() {
        if (attachedEntity == null) {
            int id = entityData.get(ENTITY_ATTACHED_TO);
            if (id == -1) {
                return null;
            }
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity) {
                attachedEntity = (LivingEntity) entity;
            }
        }
        return attachedEntity;
    }
    
    public boolean isAttachedToAnEntity() {
        return entityData.get(ENTITY_ATTACHED_TO) > -1;
    }
    
    private final Set<Entity> dragged = new HashSet<>();
    protected void dragTarget(Entity entity, Vector3d vec) {
        entity = entity.getRootVehicle();
        doDragEntity(entity, vec);
        if (entity instanceof StandEntity) {
            LivingEntity standUser = ((StandEntity) entity).getUser();
            if (standUser != null) {
                doDragEntity(entity, vec);
            }
        }
    }
    
    private void doDragEntity(Entity entity, Vector3d vec) {
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;
            for (Effect effect : target.getActiveEffectsMap().keySet()) {
                if (effect instanceof ImmobilizeEffect && ((ImmobilizeEffect) effect).resetsDeltaMovement()) {
                    entity.move(MoverType.PLAYER, vec);
                    return;
                }
            }
        }
        entity.setDeltaMovement(vec);
        dragged.add(entity);
    }
    
    public void attachToBlockPos(BlockPos blockPos) {
        entityData.set(BLOCK_ATTACHED_TO, Optional.of(blockPos));
    }
    
    public Optional<BlockPos> getBlockPosAttachedTo() {
        return entityData.get(BLOCK_ATTACHED_TO);
    }
    
    protected void setDistance(double distance) {
        this.distance = distance;
    }
    
    protected double getDistance() {
        return distance;
    }
    
    protected void setIsMovingForward(boolean isMovingForward) {
        entityData.set(IS_MOVING_FORWARD, isMovingForward);
    }
    
    protected boolean isMovingForward() {
        return entityData.get(IS_MOVING_FORWARD);
    }
    
    protected void setIsRetracting(boolean isRetracting) {
        entityData.set(IS_RETRACTING, isRetracting);
    }
    
    protected boolean isRetracting() {
        return entityData.get(IS_RETRACTING);
    }
    
    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
    
    @Override
    public int ticksLifespan() {
        return lifeSpan;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_BOUND_TO_OWNER, true);
        entityData.define(ENTITY_ATTACHED_TO, -1);
        entityData.define(BLOCK_ATTACHED_TO, Optional.empty());
        entityData.define(IS_MOVING_FORWARD, true);
        entityData.define(IS_RETRACTING, false);
    }
    
    @Override
    public boolean isInvisible() {
        boolean ownerInvisible = false;
        if (ownerInvisibility()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerInvisible = owner.isInvisible();
            }
        }
        return ownerInvisible || super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        boolean ownerInvisible = false;
        if (ownerInvisibility()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerInvisible = owner.isInvisibleTo(player);
            }
        }
        return ownerInvisible || super.isInvisibleTo(player);
    }
    
    public boolean ownerInvisibility() {
        return isBodyPart();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return isBoundToOwner() && getOwner() == ClientUtil.getClientPlayer() ? true : super.shouldRenderAtSqrDistance(distance);
    }
    
    @Override
    public SoundCategory getSoundSource() {
        return isBoundToOwner() && getOwner() != null ? getOwner().getSoundSource() : super.getSoundSource();
    }
    
    @Override
    public boolean isSilent() {
        return isBoundToOwner() && getOwner() != null ? getOwner().isSilent() : super.isSilent();
    }
    
    @Override
    public boolean canUpdate() {
        return isBoundToOwner() && getOwner() != null ? getOwner().canUpdate() : super.canUpdate();
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("BoundToOwner", isBoundToOwner());
        Optional<BlockPos> blockAttachedTo = getBlockPosAttachedTo();
        if (blockAttachedTo.isPresent()) {
            BlockPos pos = blockAttachedTo.get();
            nbt.putIntArray("AttachedBlock", new int[] { pos.getX(), pos.getY(), pos.getZ() } );
        }
        else if (attachedEntity != null) {
            nbt.putUUID("AttachedEntity", attachedEntity.getUUID());
        }
        nbt.putDouble("Distance", getDistance());
        nbt.putBoolean("IsMovingForward", isMovingForward());
        nbt.putBoolean("IsRetracting", isRetracting());
        nbt.putInt("LifeSpan", lifeSpan);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        setBoundToOwner(nbt.getBoolean("BoundToOwner"));
        int[] posArray = nbt.getIntArray("AttachedBlock");
        if (posArray.length == 3) {
            attachToBlockPos(new BlockPos(posArray[0], posArray[1], posArray[2]));
        }
        else if (nbt.hasUUID("BoundTarget")) {
            this.attachedEntityUUID = nbt.getUUID("AttachedEntity");
        }
        setDistance(nbt.getDouble("Distance"));
        setIsMovingForward(nbt.getBoolean("IsMovingForward"));
        setIsRetracting(nbt.getBoolean("IsRetracting"));
        lifeSpan = nbt.getInt("LifeSpan");
     }
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeVarInt(lifeSpan);
        buffer.writeDouble(distance);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        lifeSpan = additionalData.readVarInt();
        distance = additionalData.readDouble();
    }
}
