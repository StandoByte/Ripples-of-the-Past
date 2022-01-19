package com.github.standobyte.jojo.entity;

import java.util.UUID;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.SoulController;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRemovePlayerSoulEntityPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class SoulEntity extends Entity implements IEntityAdditionalSpawnData {
    private LivingEntity originEntity;
    private UUID originUuid;
    private int lifeSpan;
    
    public SoulEntity(World world, LivingEntity originEntity, int lifeSpan) {
        this(ModEntityTypes.SOUL.get(), world);
        setOriginEntity(originEntity);
        this.lifeSpan = lifeSpan;
    }

    public SoulEntity(EntityType<?> type, World world) {
        super(type, world);
        noPhysics = true;
    }
    
    private void setOriginEntity(LivingEntity entity) {
        this.originEntity = entity;
        if (entity != null) {
            copyPosition(entity);
            if (!level.isClientSide() && entity instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) entity).displayClientMessage(
                        new TranslationTextComponent("jojo.message.skip_soul_ascension", new KeybindTextComponent("key.jump")), true);
            }
        }
    }
    
    public LivingEntity getOriginEntity() {
        return originEntity;
    }
    
    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
    
    public int getLifeSpan() {
        return lifeSpan;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (originEntity == null || originEntity.removed
                || tickCount > 1 && !originEntity.isDeadOrDying() || tickCount > lifeSpan) {
            remove();
            return;
        }
        if (level.isClientSide()) {
            if (tickCount % 10 == 5) {
                level.addParticle(ParticleTypes.POOF, 
                        originEntity.getRandomX(1.0D), originEntity.getY(random.nextDouble() * 0.25D), originEntity.getRandomZ(1.0D), 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D);
            }
        }
        if (level.isClientSide() && isControlledByLocalInstance()) {
            // FIXME (soul) sync rotation
            this.xRot = originEntity.xRot;
            this.yRot = originEntity.yRot % 360F;
        }
        originEntity.deathTime = Math.min(originEntity.deathTime, 18);
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    public boolean isControlledByLocalInstance() {
        if (super.isControlledByLocalInstance()) {
            return true;
        }
        return level.isClientSide() && originEntity instanceof PlayerEntity && ((PlayerEntity) originEntity).isLocalPlayer();
    }
    
    private void addCloudParticles() {
        if (!isInvisibleTo(ClientUtil.getClientPlayer())) {
            for (int i = 0; i < 20; ++i) {
                level.addParticle(ModParticles.SOUL_CLOUD.get(), 
                        getRandomX(1.0D), getRandomY(), getRandomZ(1.0D), 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D, 
                        random.nextGaussian() * 0.02D);
            }
        }
    }
    
    @Override
    public void remove() {
        if (level.isClientSide()) {
            addCloudParticles();
        }
        super.remove();
    }
    
    public void skipAscension() {
        if (level.isClientSide()) {
            PacketManager.sendToServer(new ClRemovePlayerSoulEntityPacket(getId()));
        }
        tickCount = lifeSpan - 1;
    }

    private static final Vector3d UPWARDS_MOVEMENT = new Vector3d(0, 0.04D, 0);
    @Override
    public Vector3d getDeltaMovement() {
        return UPWARDS_MOVEMENT;
    }

    @Override
    protected void defineSynchedData() {}
    
    @Override
    public boolean isInvisible() {
        return true;
    }

    public boolean invisibleFlag() {
        return super.isInvisible();
    }
    
    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return !player.isSpectator() && (!ClientUtil.shouldStandsRender(player) || invisibleFlag());
    }

    @Override
    public EntitySize getDimensions(Pose pose) {
        if (originEntity != null) {
            return originEntity.getDimensions(pose);
        }
        return super.getDimensions(pose);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.tickCount = nbt.getInt("Age");
        this.lifeSpan = nbt.getInt("LifeSpan");
        if (nbt.hasUUID("Origin")) {
            this.originUuid = nbt.getUUID("Origin");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
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
            Entity entity = ((ServerWorld) level).getEntity(originUuid);
            if (entity instanceof LivingEntity) {
                setOriginEntity((LivingEntity) entity);
            }
            else {
                remove();
            }
        }
        buffer.writeInt(originEntity == null ? -1 : originEntity.getId());
        buffer.writeInt(lifeSpan);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            setOriginEntity((LivingEntity) entity);
            SoulController.onSoulSpawn(this);
            addCloudParticles();
        }
        else {
            remove();
            return;
        }
        lifeSpan = additionalData.readInt();
    }
}
