package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class HGBarrierEntity extends OwnerBoundProjectileEntity {
    protected static final DataParameter<Boolean> WAS_RIPPED = EntityDataManager.defineId(HGBarrierEntity.class, DataSerializers.BOOLEAN);
    private LivingEntity standUser;
    private BlockPos originBlockPos;
    private int rippedTicks = -1;
    private boolean timeStop = false;
    
    public HGBarrierEntity(World world, StandEntity entity) {
        super(ModEntityTypes.HG_BARRIER.get(), entity, world);
        this.standUser = entity.getUser();
    }
    
    public HGBarrierEntity(EntityType<? extends HGBarrierEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public void setOriginBlockPos(BlockPos blockPos) {
        this.originBlockPos = blockPos;
    }
    
    public BlockPos getOriginBlockPos() {
        return originBlockPos;
    }
    
    @Override
    public void tick() {
        if (!timeStop) {
            if (rippedTicks > 0) {
                if (--rippedTicks == 0) {
                    remove();
                    return;
                }
            }
            else {
                if (standUser == null) {
                    remove();
                    return;
                }
                super.tick();
                if (!isAlive()) {
                    return;
                }
            }
        }
        else if (!level.isClientSide() && !wasRipped()) {
            RayTraceResult[] rayTrace = rayTrace();
            for (RayTraceResult result : rayTrace) {
                if (result.getType() == RayTraceResult.Type.ENTITY && !ForgeEventFactory.onProjectileImpact(this, result)) {
                    entityData.set(WAS_RIPPED, true);
                    break;
                }
            }
        }
    }

    @Override
    protected boolean moveBoundToOwner() {
        moveTo(getOwner().getEyePosition(1.0F).add(getOwnerRelativeOffset()));
        return true;
    }
    
    @Override
	public boolean isBodyPart() {
        return true;
    }
    
    @Override
    public Vector3d getOriginPoint(float partialTick) {
        return originBlockPos == null
                ? !level.isClientSide() || standUser == null ? position() : standUser.getPosition(partialTick)
                        : Vector3d.atCenterOf(originBlockPos);
    }

    @Deprecated
    @Override
    protected Vector3d getNextOriginOffset() { return Vector3d.ZERO; }
    
    @Override
    protected void onHitBlock(BlockRayTraceResult result) {}

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(WAS_RIPPED, false);
    }
    
    private static final Vector3d OFFSET = new Vector3d(0.15D, -1.4D, 0);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> dataParameter) {
        super.onSyncedDataUpdated(dataParameter);
        if (WAS_RIPPED.equals(dataParameter) && wasRipped()) {
            rippedTicks = 40;
        }
    }
    
    public boolean wasRipped() {
        return entityData.get(WAS_RIPPED);
    }
    
    @Override
    public boolean isGlowing() {
        return level.isClientSide() && !timeStop && wasRipped() && getOwner() instanceof StandEntity && 
                ((StandEntity) getOwner()).getUser() == ClientUtil.getClientPlayer() || super.isGlowing();
    }
    
    @Override
    public int getTeamColor() {
        return wasRipped() ? 0xFF0000 : super.getTeamColor();
    }
    
    @Override
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult) {
        if (getBlockPosAttachedTo().isPresent()) {
            Entity target = entityRayTraceResult.getEntity();
            target.setDeltaMovement(target.getDeltaMovement().scale(0.1D));
            if (!level.isClientSide()) {
                super.onHitEntity(entityRayTraceResult);
                entityData.set(WAS_RIPPED, true);
                if (getOwner() instanceof HierophantGreenEntity) {
                    HierophantGreenEntity stand = (HierophantGreenEntity) getOwner();
                    stand.shootEmeraldsFromBarriers(entityRayTraceResult.getLocation(), 3);
                    if (stand.getUser() != null) {
                        stand.getUser().hurt(DamageSource.GENERIC, 0.1F);
                    }
                }
                JojoModUtil.playSound(level, null, target.getX(), target.getY(), target.getZ(), 
                        ModSounds.HIEROPHANT_GREEN_BARRIER_RIPPED.get(), getSoundSource(), 1.0F, 1.0F, StandUtil::isEntityStandUser);
            }
        }
    }
    
    public void shootEmeralds(Vector3d targetPos, int count) {
        Vector3d barrierMiddle = position().add(getOriginPoint()).scale(0.5);
        for (int i = 0; i < count; i++) {
            HGEmeraldEntity emeraldEntity = new HGEmeraldEntity(getOwner(), level, null);
            emeraldEntity.setPos(barrierMiddle.x, barrierMiddle.y, barrierMiddle.z);
            emeraldEntity.setConcentrated(true);
            Vector3d shootVec = targetPos.subtract(barrierMiddle);
            emeraldEntity.shoot(shootVec.x, shootVec.y, shootVec.z, 1F, 2.0F);
            level.addFreshEntity(emeraldEntity);
//            JojoModUtil.playSound(level, null, barrierMiddle.x, barrierMiddle.y, barrierMiddle.z, 
//                    ModEntityTypes.HIEROPHANT_GREEN.get().getSound(StandSoundType.RANGED_ATTACK), 
//                    getOwner().getSoundSource(), 1.0F, 1.0F, StandUtil::isPlayerStandUser);
        }
    }

    @Override
    protected int ticksLifespan() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    protected float movementSpeed() {
        return 0F;
    }

    @Override
    public float getBaseDamage() {
        return 2.0F;
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public void canUpdate(boolean canUpdate) {
        timeStop = !canUpdate;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
    }
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(standUser.getId());
        boolean isBlockOrigin = originBlockPos != null;
        buffer.writeBoolean(isBlockOrigin);
        if (isBlockOrigin) {
            buffer.writeBlockPos(originBlockPos);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            standUser = (LivingEntity) entity;
        }
        if (additionalData.readBoolean()) {
            originBlockPos = additionalData.readBlockPos();
        }
    }

}
