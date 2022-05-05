package com.github.standobyte.jojo.entity;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class AfterimageEntity extends Entity implements IEntityAdditionalSpawnData {
    private Entity originEntity;
    private UUID originUuid;
    private int ticksDelayed;
    private int delay;
    private int lifeSpan;
    private Queue<PosData> originPosQueue = new LinkedList<PosData>();
    
    public AfterimageEntity(World world, Entity originEntity, int delay) {
        this(ModEntityTypes.AFTERIMAGE.get(), world);
        setOriginEntity(originEntity);
        this.delay = delay;
        this.lifeSpan = 1200;
    }

    public AfterimageEntity(EntityType<?> type, World world) {
        super(type, world);
        noPhysics = true;
    }
    
    private void setOriginEntity(Entity entity) {
        this.originEntity = entity;
        if (entity != null) {
            copyPosition(entity);
        }
    }
    
    public Entity getOriginEntity() {
        return originEntity;
    }
    
    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
    
    @Override
    public void tick() {
        super.tick();
        ticksDelayed++;
        if (originEntity == null || !originEntity.isAlive() || tickCount > lifeSpan) {
            if (!level.isClientSide()) {
                remove();
            }
            return;
        }
        originPosQueue.add(new PosData(originEntity.position(), originEntity.xRot, originEntity.yRot));
        if (ticksDelayed > delay) {
            PosData posData = originPosQueue.remove();
            moveTo(posData.pos.x, posData.pos.y, posData.pos.z, posData.yRot, posData.xRot);
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.delay = nbt.getInt("Delay");
        this.tickCount = nbt.getInt("Age");
        this.lifeSpan = nbt.getInt("LifeSpan");
        if (nbt.hasUUID("Origin")) {
            this.originUuid = nbt.getUUID("Origin");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("Delay", delay);
        nbt.putInt("Age", tickCount);
        nbt.putInt("LifeSpan", lifeSpan);
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
            setOriginEntity(((ServerWorld) level).getEntity(originUuid));
        }
        buffer.writeInt(originEntity == null ? -1 : originEntity.getId());
        buffer.writeInt(delay);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        setOriginEntity(level.getEntity(additionalData.readInt()));
        delay = additionalData.readInt();
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
