package com.github.standobyte.jojo.entity;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class MRDetectorEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final DataParameter<Boolean> ENTITY_DETECTED = EntityDataManager.defineId(MRDetectorEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> DETECTED_X = EntityDataManager.defineId(MRDetectorEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DETECTED_Y = EntityDataManager.defineId(MRDetectorEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DETECTED_Z = EntityDataManager.defineId(MRDetectorEntity.class, DataSerializers.FLOAT);
    private LivingEntity owner;
    
    public MRDetectorEntity(LivingEntity owner, World world) {
        this(ModEntityTypes.MR_DETECTOR.get(), world);
        this.owner = owner;
    }
    
    public MRDetectorEntity(EntityType<?> type, World world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
            return;
        }
        super.tick();
        if (tickCount < 600 && owner != null && owner.isAlive()) {
            Vector3d newPos = owner.getEyePosition(1.0F).add(MathUtil.relativeCoordsToAbsolute(-0.5, -0.5, 1.5, owner.yRot));
            setPos(newPos.x, newPos.y, newPos.z);
        }
        else {
            if (!level.isClientSide()) remove();
            return;
        }
        if (!level.isClientSide()) {
            Vector3d detectedOffset = detectEntities();
            setDetectedOffset(detectedOffset);
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerWorld) level);
        }
    }
    
    public static final double DETECTION_RADIUS = 15;
    @Nullable
    private Vector3d detectEntities() {
        AxisAlignedBB aabb = new AxisAlignedBB(position(), position()).inflate(DETECTION_RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, 
                EntityPredicates.LIVING_ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS)
                .and(entity -> entity.getType() != EntityType.ARMOR_STAND && entity != owner && 
                (owner == null || 
                !(entity.isAlliedTo(owner) || IStandPower.getStandPowerOptional(owner).map(stand -> entity == stand.getStandManifestation()).orElse(false)))));
        Optional<LivingEntity> closestDetected = entities.stream().min((e1, e2) -> (int) (e1.distanceToSqr(this) - e2.distanceToSqr(this)));
        closestDetected.ifPresent(entity -> {
            if (this.getBoundingBox().intersects(entity.getBoundingBox())) {
                DamageUtil.setOnFire(entity, 4, true);
            }
        });
        return closestDetected.isPresent() ? closestDetected.get().position().subtract(position()) : null;
    }
    
    private void setDetectedOffset(@Nullable Vector3d detected) {
        if (detected == null) {
            entityData.set(ENTITY_DETECTED, false);
        }
        else {
            entityData.set(ENTITY_DETECTED, true);
            entityData.set(DETECTED_X, (float) detected.x);
            entityData.set(DETECTED_Y, (float) detected.y);
            entityData.set(DETECTED_Z, (float) detected.z);
        }
    }
    
    @Override
    public void onSyncedDataUpdated(DataParameter<?> parameter) {
        if (level.isClientSide() && ENTITY_DETECTED.equals(parameter) && isEntityDetected()
                && ClientUtil.canHearStands()) {
            ClientTickingSoundsHelper.playMagiciansRedDetectorSound(this);
        }
        super.onSyncedDataUpdated(parameter);
    }
    
    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return !StandUtil.clStandEntityVisibleTo(player) || !player.isSpectator() && super.isInvisible();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ENTITY_DETECTED, false);
        entityData.define(DETECTED_X, 0F);
        entityData.define(DETECTED_Y, 0F);
        entityData.define(DETECTED_Z, 0F);
    }
    
    public boolean isEntityDetected() {
        return entityData.get(ENTITY_DETECTED);
    }
    
    public Vector3f getDetectedDirection() {
        return new Vector3f(entityData.get(DETECTED_X), entityData.get(DETECTED_Y), entityData.get(DETECTED_Z));
    }
    
    public Entity getOwner() {
        return owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        // no save
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        // no save
    }
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(owner != null ? owner.getId() : -1);
    }
    
    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity owner = level.getEntity(additionalData.readInt());
        if (owner instanceof LivingEntity) {
            this.owner = (LivingEntity) owner;
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
}
