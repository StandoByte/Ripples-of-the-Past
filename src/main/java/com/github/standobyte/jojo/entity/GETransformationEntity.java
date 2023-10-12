package com.github.standobyte.jojo.entity;

import java.io.IOException;
import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

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
        
        
        double f = getEyeHeight() - 0.11111111;
        Vector3d deltaMovement = getDeltaMovement();
        if (isInWater() && getFluidHeight(FluidTags.WATER) > f) {
            setDeltaMovement(
                    deltaMovement.x * 0.99, 
                    deltaMovement.y + (deltaMovement.y < 0.06 ? 5.0E-4 : 0), 
                    deltaMovement.z * 0.99);
        }
        else if (isInLava() && getFluidHeight(FluidTags.LAVA) > f) {
            setDeltaMovement(
                    deltaMovement.x * 0.95, 
                    deltaMovement.y + (deltaMovement.y < 0.06 ? 5.0E-4 : 0), 
                    deltaMovement.z * 0.95);
        }
        else if (!isNoGravity()) {
            setDeltaMovement(deltaMovement.add(0, -0.04, 0));
        }
        
        if (!onGround || getHorizontalDistanceSqr(getDeltaMovement()) > 1.0E-5 || (tickCount + getId()) % 4 == 0) {
            move(MoverType.SELF, getDeltaMovement());
            double inertia = 0.98;
            if (onGround) {
                inertia = level.getBlockState(new BlockPos(getX(), getY() - 1.0, getZ()))
                        .getSlipperiness(level, new BlockPos(getX(), getY() - 1.0, getZ()), this) * 0.98;
            }
            
            setDeltaMovement(getDeltaMovement().multiply(inertia, 0.98, inertia));
            if (onGround) {
                deltaMovement = getDeltaMovement();
                if (deltaMovement.y < 0.0D) {
                    setDeltaMovement(deltaMovement.multiply(1.0, -0.5, 1.0));
                }
            }
        }
        
        
        refreshDimensions();
        
        super.tick();
    }
    
    // Mojang?!?
    @Override
    public void refreshDimensions() {
        double x = getX();
        double y = getY();
        double z = getZ();
        super.refreshDimensions(); // why does it shift the entity along the XZ axes when it increases in size anyway?...
        this.setPos(x, y, z);
    }
    
    @Override
    public EntitySize getDimensions(Pose pPose) {
        EntitySize size = new EntitySize(getBbWidth(), getBbHeight(), false);
        float scale = 0;
        
        float renderAsItemTime = getRenderAsItemTime(duration);
        if (tickCount < renderAsItemTime) {
            if (source != null) {
                scale = 1 - tickCount / renderAsItemTime;
                if (scale > 0) {
                    size = source.getDimensions(pPose).scale(scale);
                }
            }
        }
        
        else if (target != null) {
            scale = 1 - (duration - tickCount) / (duration - renderAsItemTime);
            if (scale > 0) {
                size = target.getDimensions(pPose).scale(scale);
            }
        }
        
        return size;
    }
    
    public static float getRenderAsItemTime(float fullDuration) {
        return Math.min(fullDuration / 3, 20);
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
        
        if (nbt.contains("SourceEntity", MCUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT entityNbt = nbt.getCompound("SourceEntity");
            source = EntityType.create(entityNbt, level).orElse(null);
        }
        if (nbt.contains("TargetEntity", MCUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT entityNbt = nbt.getCompound("TargetEntity");
            target = EntityType.create(entityNbt, level).orElse(null);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("Duration", duration);
        
        if (source != null) {
            CompoundNBT entityNbt = source.serializeNBT();
            nbt.put("SourceEntity", entityNbt);
        }
        if (target != null) {
            CompoundNBT entityNbt = target.serializeNBT();
            nbt.put("TargetEntity", entityNbt);
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeVarInt(tickCount);
        buffer.writeVarInt(duration);
        
        writeEntityData(buffer, target);
        writeEntityData(buffer, source);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        tickCount = additionalData.readVarInt();
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
