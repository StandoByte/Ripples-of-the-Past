package com.github.standobyte.jojo.entity;

import java.util.UUID;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.SoulController;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRemovePlayerSoulEntityPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSoulRotationPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.command.arguments.EntityAnchorArgument;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
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
    private boolean givesResolve;
    
    public SoulEntity(World world, LivingEntity originEntity, int lifeSpan, boolean givesResolve) {
        this(ModEntityTypes.SOUL.get(), world);
        setOriginEntity(originEntity);
        this.lifeSpan = lifeSpan;
        this.givesResolve = givesResolve;
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
            entity.setRemainingFireTicks(-20);
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
        else if (givesResolve) {
            level.getEntitiesOfClass(LivingEntity.class, 
                    new AxisAlignedBB(getBoundingBox().getCenter(), getBoundingBox().getCenter()).inflate(24), 
                    entity -> !entity.is(originEntity) && originEntity.isAlliedTo(entity)).forEach(entity -> {
                        IStandPower.getStandPowerOptional(entity).ifPresent(stand -> 
                        stand.getResolveCounter().soulAddResolveTeammate());
                    });
            RayTraceResult rayTrace = JojoModUtil.rayTrace(this, 32, entity -> !entity.is(originEntity), 1.0);
            if (rayTrace.getType() == RayTraceResult.Type.ENTITY) {
                Entity lookEntity = ((EntityRayTraceResult) rayTrace).getEntity();
                if (lookEntity instanceof LivingEntity) {
                    IStandPower.getStandPowerOptional(lookEntity instanceof StandEntity ? ((StandEntity) lookEntity).getUser() : (LivingEntity) lookEntity)
                    .ifPresent(stand -> stand.getResolveCounter().soulAddResolveLook());
                }
            }
        }
        if (this.isClientPlayer()) {
            setRot(originEntity.yRot, originEntity.xRot);
            setYHeadRot(originEntity.yRot);
        }
        
        tickRotation();
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
        return !player.isSpectator() && !player.is(originEntity) && (!StandUtil.shouldStandsRender(player) || invisibleFlag());
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
        this.givesResolve = nbt.getBoolean("Resolve");
        if (nbt.hasUUID("Origin")) {
            this.originUuid = nbt.getUUID("Origin");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("LifeSpan", lifeSpan);
        nbt.putBoolean("Resolve", givesResolve);
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
            SoulController.getInstance().onSoulSpawn(this);
            addCloudParticles();
        }
        else {
            remove();
            return;
        }
        lifeSpan = additionalData.readInt();
    }
    
    // rotation stuff copied from LivingEntity

    public float yBodyRot;
    public float yBodyRotO;
    public float yHeadRot;
    public float yHeadRotO;
    protected float animStep;
    protected float animStepO;
    protected int lerpSteps;
    protected double lerpYRot;
    protected double lerpXRot;
    protected double lyHeadRot;
    protected int lerpHeadSteps;
    
    private void tickRotation() {
        this.aiStep();
        double d0 = this.getX() - this.xo;
        double d1 = this.getZ() - this.zo;
        float horizontalDistSqr = (float)(d0 * d0 + d1 * d1);
        float f1 = this.yBodyRot;
        float f2 = 0.0F;
        if (horizontalDistSqr > 0.0025F) {
            f2 = (float)Math.sqrt((double)horizontalDistSqr) * 3.0F;
            float f4 = (float)MathHelper.atan2(d1, d0) * (180F / (float)Math.PI) - 90.0F;
            float f5 = MathHelper.abs(MathHelper.wrapDegrees(this.yRot) - f4);
            if (95.0F < f5 && f5 < 265.0F) {
                f1 = f4 - 180.0F;
            } else {
                f1 = f4;
            }
        }

        f2 = this.tickHeadTurn(f1, f2);

        while(this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO < -180.0F) {
            this.yBodyRotO -= 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
            this.yBodyRotO += 360.0F;
        }

        while(this.xRot - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while(this.xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO < -180.0F) {
            this.yHeadRotO -= 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO >= 180.0F) {
            this.yHeadRotO += 360.0F;
        }
        this.animStep += f2;
        
        if (isClientPlayer()) {
            this.sendPosition();
        }
    }
    
    @Override
    public void baseTick() {
        super.baseTick();
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    protected float tickHeadTurn(float p_110146_1_, float p_110146_2_) {
        float f = MathHelper.wrapDegrees(p_110146_1_ - this.yBodyRot);
        this.yBodyRot += f * 0.3F;
        float f1 = MathHelper.wrapDegrees(this.yRot - this.yBodyRot);
        boolean flag = f1 < -90.0F || f1 >= 90.0F;
        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        this.yBodyRot = this.yRot - f1;
        if (f1 * f1 > 2500.0F) {
            this.yBodyRot += f1 * 0.2F;
        }

        if (flag) {
            p_110146_2_ *= -1.0F;
        }

        return p_110146_2_;
    }

    public void aiStep() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            this.yRot = (float)((double)this.yRot + MathHelper.wrapDegrees(this.lerpYRot - (double)this.yRot) / (double)this.lerpSteps);
            this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
            --this.lerpSteps;
            this.setRot(this.yRot, this.xRot);
        }

        if (this.lerpHeadSteps > 0) {
            this.yHeadRot = (float)((double)this.yHeadRot + MathHelper.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
            --this.lerpHeadSteps;
        }

        if (this.isEffectiveAi()) {
            this.serverAiStep();
        }
    }

    public boolean isEffectiveAi() {
        return !this.level.isClientSide || isClientPlayer();
    }
    
    private boolean isClientPlayer() {
        return this.level.isClientSide && originEntity.is(ClientUtil.getClientPlayer());
    }

    protected void serverAiStep() {
        this.yHeadRot = this.yRot;
    }

    public void lerpTo(double p_180426_1_, double p_180426_3_, double p_180426_5_, float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
        this.lerpYRot = (double)p_180426_7_;
        this.lerpXRot = (double)p_180426_8_;
        this.lerpSteps = p_180426_9_;
    }

    public void lerpHeadTo(float p_208000_1_, int p_208000_2_) {
        this.lyHeadRot = (double)p_208000_1_;
        this.lerpHeadSteps = p_208000_2_;
    }

    @Override
    public float getViewYRot(float p_195046_1_) {
        return p_195046_1_ == 1.0F ? this.yHeadRot : MathHelper.lerp(p_195046_1_, this.yHeadRotO, this.yHeadRot);
    }

    public float getYHeadRot() {
        return this.yHeadRot;
    }

    public void setYHeadRot(float p_70034_1_) {
        this.yHeadRot = p_70034_1_;
    }

    public void setYBodyRot(float p_181013_1_) {
        this.yBodyRot = p_181013_1_;
    }

    public void lookAt(EntityAnchorArgument.Type p_200602_1_, Vector3d p_200602_2_) {
        super.lookAt(p_200602_1_, p_200602_2_);
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRot = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
    }

    private float yRotLast;
    private float xRotLast;

    private void sendPosition() {
        if (isClientPlayer()) {
            float d2 = this.yRot - this.yRotLast;
            float d3 = this.xRot - this.xRotLast;
            boolean flag2 = d2 != 0.0F || d3 != 0.0F;
            PacketManager.sendToServer(new ClSoulRotationPacket(getId(), this.yRot, this.xRot));

            if (flag2) {
                this.yRotLast = this.yRot;
                this.xRotLast = this.xRot;
            }
        }
    }
    
    public void handleRotationPacket(float msgYRot, float msgXRot) {
        this.yRot = msgYRot % 360.0F;
        this.xRot = MathHelper.clamp(msgXRot, -90.0F, 90.0F) % 360.0F;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }
}
