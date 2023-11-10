package com.github.standobyte.jojo.entity;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class AfterimageEntity extends Entity implements IEntityAdditionalSpawnData {
    private LivingEntity originEntity;
    private UUID originUuid;
    private int ticksDelayed;
    private int delay;
    private int lifeSpan;
    private double speedLowerLimit;
    private Queue<PosData> originPosQueue = new LinkedList<PosData>();
    
    public AfterimageEntity(World world, LivingEntity originEntity, int delay) {
        this(ModEntityTypes.AFTERIMAGE.get(), world);
        setOriginEntity(originEntity);
        this.delay = delay;
        this.lifeSpan = 1200;
    }

    public AfterimageEntity(EntityType<?> type, World world) {
        super(type, world);
        noPhysics = true;
    }
    
    private void setOriginEntity(LivingEntity entity) {
        this.originEntity = entity;
        if (entity != null) {
            copyPosition(entity);
        }
    }
    
    public LivingEntity getOriginEntity() {
        return originEntity;
    }
    
    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
    
    public void setMinSpeed(double speed) {
        this.speedLowerLimit = speed;
    }
    
    public boolean shouldRender() {
        return originEntity != null && originEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) >= speedLowerLimit;
    }
    
    @Override
    public void tick() {
        super.tick();
        ticksDelayed++;
        if (originEntity == null || !originEntity.isAlive() || !level.isClientSide() && tickCount > lifeSpan) {
            remove();
            return;
        }
        originPosQueue.add(new PosData(originEntity.position(), originEntity.xRot, originEntity.yRot));
        if (ticksDelayed > delay) {
            PosData posData = originPosQueue.remove();
            moveTo(posData.pos.x, posData.pos.y, posData.pos.z, posData.yRot, posData.xRot);
        }
        
        if (!level.isClientSide() && originEntity.isSprinting() && shouldRender()) {
            level.getEntitiesOfClass(MobEntity.class, this.getBoundingBox().inflate(8), mob -> 
            mob.getTarget() == originEntity && mob.canSee(this)).forEach(mob -> {
                if (mob.getRandom().nextDouble() < 0.01) {
                    MCUtil.loseTarget(mob, originEntity);
                }
            });
        }
    }

    @Override
    protected void defineSynchedData() {}
    
    @Override
    public boolean isInvisible() {
        return super.isInvisible() || originEntity != null && originEntity.isInvisible();
    }
    
    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return super.isInvisibleTo(player) || originEntity != null && originEntity.isInvisibleTo(player);
    }
    
    @Override
    public boolean displayFireAnimation() {
        return originEntity != null && originEntity.displayFireAnimation();
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.delay = nbt.getInt("Delay");
        this.tickCount = nbt.getInt("Age");
        this.lifeSpan = nbt.getInt("LifeSpan");
        this.speedLowerLimit = nbt.getDouble("Speed");
        if (nbt.hasUUID("Origin")) {
            this.originUuid = nbt.getUUID("Origin");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("Delay", delay);
        nbt.putInt("Age", tickCount);
        nbt.putInt("LifeSpan", lifeSpan);
        nbt.putDouble("Speed", speedLowerLimit);
        if (originUuid != null) {
            nbt.putUUID("Origin", originEntity.getUUID());
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        if (originUuid != null) {
            Entity entity = ((ServerWorld) level).getEntity(originUuid);
            if (entity instanceof LivingEntity) {
                setOriginEntity((LivingEntity) entity);
            }
        }
        buffer.writeInt(originEntity == null ? -1 : originEntity.getId());
        buffer.writeVarInt(delay);
        buffer.writeInt(lifeSpan);
        buffer.writeDouble(speedLowerLimit);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            setOriginEntity((LivingEntity) entity);
        }
        delay = additionalData.readVarInt();
        lifeSpan = additionalData.readInt();
        speedLowerLimit = additionalData.readDouble();
    }

    private static class PosData {
        private final Vector3d pos;
        private final float xRot;
        private final float yRot;
        
        private PosData(Vector3d pos, float xRot, float yRot) {
            this.pos = pos;
            this.xRot = xRot;
            this.yRot = yRot;
        }
    }
}
