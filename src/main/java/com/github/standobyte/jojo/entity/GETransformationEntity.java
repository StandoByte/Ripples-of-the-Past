package com.github.standobyte.jojo.entity;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
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
    private EntityOwnerResolver owner = new EntityOwnerResolver();
    private int duration;
    private boolean isTurningBack = false;

    public GETransformationEntity(EntityType<?> type, World level) {
        super(type, level);
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
    
    public GETransformationEntity withOwner(LivingEntity user) {
        this.owner.setOwner(user);
        return this;
    }
    
    public GETransformationEntity withDuration(int duration) {
        this.duration = duration;
        return this;
    }
    
    public GETransformationEntity setTurningBack() {
        this.isTurningBack = true;
        return this;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public boolean isTurningBack() {
        return isTurningBack;
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
                Entity entityToSummon = isTurningBack ? source : target;
                if (entityToSummon != null) {
                    entityToSummon.copyPosition(this);
                    level.addFreshEntity(entityToSummon);
                    GoldExperienceCreateLifeform.onTransformationFinish(entityToSummon);
                    
                    if (!isTurningBack && entityToSummon instanceof LivingEntity) {
                        IStandPower.getStandPowerOptional(owner.getEntity(level)).ifPresent(power -> {
                            power.getContinuousEffects().addEffect(new GECreatedLifeformEffect()
                                    .withOriginalEntity(source)
                                    .withStand(power)
                                    .withTarget((LivingEntity) entityToSummon)); 
                        });
                    }
                }
            }
            return;
        }
        else {
            if (!level.isClientSide()) {
                
            }
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
        float tfProgressTime = getTfProgressTime(0);
        if (tfProgressTime < renderAsItemTime) {
            if (source != null) {
                scale = 1 - tfProgressTime / renderAsItemTime;
                if (scale > 0) {
                    size = source.getDimensions(pPose).scale(scale);
                }
            }
        }
        
        else if (target != null) {
            scale = 1 - (duration - tfProgressTime) / (duration - renderAsItemTime);
            if (scale > 0) {
                size = target.getDimensions(pPose).scale(scale);
            }
        }
        
        return size;
    }
    
    public float getTfProgressTime(float partialTick) {
        float time = tickCount + partialTick;
        return isTurningBack ? duration - time : time;
    }
    
    public static float getRenderAsItemTime(float fullDuration) {
        return Math.min(fullDuration / 3, 20);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(LIFE_FORM_SPAWNED, false);
    }
    
    @Override
    public boolean isInvisible() {
        return super.isInvisible() || entityData.get(LIFE_FORM_SPAWNED);
    }
    
    
    public static void turnEntityBack(Entity lifeform, Entity originalEntity, @Nullable LivingEntity owner) {
        Entity tf = new GETransformationEntity(lifeform.level)
                .withTransformationTarget(lifeform)
                .withTransformationSource(originalEntity)
                .withDuration(10)
                .withOwner(owner)
                .setTurningBack();
        
        Vector3d pos = lifeform.position();
        tf.moveTo(pos.x, pos.y, pos.z, lifeform.yRot, lifeform.xRot);
        lifeform.level.addFreshEntity(tf);
        
        lifeform.remove();
    }
    
    
    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.tickCount = nbt.getInt("Age");
        this.duration = nbt.getInt("Duration");
        this.isTurningBack = nbt.getBoolean("TurnBack");
        
        if (nbt.contains("SourceEntity", MCUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT entityNbt = nbt.getCompound("SourceEntity");
            source = EntityType.create(entityNbt, level).orElse(null);
        }
        if (nbt.contains("TargetEntity", MCUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT entityNbt = nbt.getCompound("TargetEntity");
            target = EntityType.create(entityNbt, level).orElse(null);
        }
        owner.loadNbt(nbt, "Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("Duration", duration);
        nbt.putBoolean("TurnBack", isTurningBack);
        
        if (source != null) {
            CompoundNBT entityNbt = source.serializeNBT();
            nbt.put("SourceEntity", entityNbt);
        }
        if (target != null) {
            CompoundNBT entityNbt = target.serializeNBT();
            nbt.put("TargetEntity", entityNbt);
        }
        owner.saveNbt(nbt, "Owner");
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeVarInt(tickCount);
        buffer.writeVarInt(duration);
        buffer.writeBoolean(isTurningBack);
        owner.writeNetwork(buffer);
        
        writeEntityData(buffer, target);
        writeEntityData(buffer, source);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        tickCount = additionalData.readVarInt();
        duration = additionalData.readVarInt();
        isTurningBack = additionalData.readBoolean();
        owner.readNetwork(additionalData);
        
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
