package com.github.standobyte.jojo.entity;

import java.io.IOException;
import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

//FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! match the target entity hitbox size
public class GETransformationEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final DataParameter<Boolean> LIFE_FORM_SPAWNED = EntityDataManager.defineId(GETransformationEntity.class, DataSerializers.BOOLEAN);
    private Entity source;
    private Entity target;
    private int duration;

    public GETransformationEntity(EntityType<?> type, World level) {
        super(type, level);
        source = new ItemEntity(level, 0, 0, 0, new ItemStack(Items.DIRT));
    }

    public GETransformationEntity(World pLevel) {
        this(ModEntityTypes.GE_LIFEFORM_TRANSFORMATION.get(), pLevel);
    }
    
    public GETransformationEntity withTransformationSource(Entity entity) {
        this.source = entity;
        return this;
    }
    
    public GETransformationEntity withTransformationTarget(Entity entity) {
        this.target = entity;
        return this;
    }
    
    public GETransformationEntity withDuration(int duration) {
        this.duration = duration;
        return this;
    }
    
    public int getDuration() {
        return duration;
    }
    
    
    public Entity getTransformationSource() {
        return source;
    }
    
    public Entity getTransformationTarget() {
        return target;
    }
    
    @Override
    public void tick() {
        if (tickCount >= duration) {
            if (!level.isClientSide()) {
                entityData.set(LIFE_FORM_SPAWNED, true);
                remove();
                if (target != null) {
                    target.copyPosition(this);
                    level.addFreshEntity(target);
                    GoldExperienceCreateLifeform.onTransformationFinish(target);
                }
            }
            return;
        }
        super.tick();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(LIFE_FORM_SPAWNED, false);
    }
    
    public boolean isInvisible() {
        return super.isInvisible() || entityData.get(LIFE_FORM_SPAWNED);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.tickCount = nbt.getInt("Age");
        this.duration = nbt.getInt("Duration");
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("Duration", duration);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeVarInt(duration);
        
        writeEntityData(buffer, target);
        writeEntityData(buffer, source);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        duration = additionalData.readVarInt();
        
        target = readEntityData(additionalData);
        source = readEntityData(additionalData);
    }
    
    private void writeEntityData(PacketBuffer buffer, Entity entityToWrite) {
        NetworkUtil.writeOptionally(buffer, entityToWrite, entity -> {
            byte pitch = (byte) MathHelper.floor(entity.xRot * 256.0F / 360.0F);
            byte yaw = (byte) MathHelper.floor(entity.yRot * 256.0F / 360.0F);
            byte headYaw = (byte) (entity.getYHeadRot() * 256.0F / 360.0F);
            
            buffer.writeRegistryId(entity.getType());
            buffer.writeByte(pitch);
            buffer.writeByte(yaw);
            buffer.writeByte(headYaw);
            
            if (entity instanceof IEntityAdditionalSpawnData) {
                ((IEntityAdditionalSpawnData) entity).writeSpawnData(buffer);
            }
            
            List<EntityDataManager.DataEntry<?>> entityData = entity.getEntityData().getAll();
            try {
                EntityDataManager.pack(entityData, buffer);
            } catch (IOException e) {
                JojoMod.LOGGER.error("Failed to write entity data for Gold Experience's transformation render for entity of type {}", entity.getType().getRegistryName());
                e.printStackTrace();
            }
        });
    }
    
    private Entity readEntityData(PacketBuffer buffer) {
        return NetworkUtil.readOptional(buffer, () -> {
            EntityType<?> type = buffer.readRegistryIdSafe(EntityType.class);
            Entity entity = type.create(level);
            
            float pitch = (buffer.readByte() * 360) / 256.0F;
            float yaw = (buffer.readByte() * 360) / 256.0F;
            float headYaw = (buffer.readByte() * 360) / 256.0F;
            
            entity.yRot = yaw % 360.0F;
            entity.xRot = MathHelper.clamp(pitch, -90.0F, 90.0F) % 360.0F;
            entity.yRotO = entity.yRot;
            entity.xRotO = entity.xRot;
            entity.setYHeadRot(headYaw);
            entity.setYBodyRot(headYaw);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.yHeadRotO = living.yHeadRot;
                living.yBodyRotO = living.yBodyRot;
            }

            if (entity instanceof IEntityAdditionalSpawnData) {
                ((IEntityAdditionalSpawnData) entity).readSpawnData(buffer);
            }
            
            try {
                List<EntityDataManager.DataEntry<?>> entityData = EntityDataManager.unpack(buffer);
                entity.getEntityData().assignValues(entityData);
            } catch (IOException e) {
                JojoMod.LOGGER.error("Failed to read entity data for Gold Experience's transformation render for entity of type {}", entity.getType().getRegistryName());
                e.printStackTrace();
            } catch (Exception e) {
                JojoMod.LOGGER.error("Failed to assign entity data for Gold Experience's transformation render for entity of type {}", entity.getType().getRegistryName());
                e.printStackTrace();
            }
            
            return entity;
        }).orElse(null);
    }

}
