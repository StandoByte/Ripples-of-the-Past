package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanDivineSandstormEntity extends ModdedProjectileEntity {
    private float radius;
    private float damage;
    private float particlesCount;
    private int duration;

    public PillarmanDivineSandstormEntity(World world, LivingEntity entity) {
        super(ModEntityTypes.PILLARMAN_DIVINE_SANDSTORM.get(), entity, world);
    }
    
    public PillarmanDivineSandstormEntity setRadius(float radius) {
        this.radius = radius;
        this.particlesCount = radius * 2;
        Vector3d pos = getBoundingBox().getCenter();
        refreshDimensions();
        setBoundingBox(new AxisAlignedBB(pos, pos).inflate(radius));
        return this;
    }
    
    public PillarmanDivineSandstormEntity setDamage(float damage) {
        this.damage = damage;
        return this;
    }
    
    public PillarmanDivineSandstormEntity setDuration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public PillarmanDivineSandstormEntity(EntityType<? extends PillarmanDivineSandstormEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        setPos(getX(), getY() - radius, getZ());
        super.shoot(x, y, z, velocity, inaccuracy);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            Vector3d center = getBoundingBox().getCenter();
            int particlesCount = Math.max((int) (this.particlesCount * damageWearOffMultiplier()), 1);
            for (int i = 0; i < particlesCount; i++) {
                Vector3d sparkVec = center.add(new Vector3d(
                        (random.nextDouble() - 1.0), 
                        (random.nextDouble() - 1.0),
                        (random.nextDouble() - 1.0))
                        .normalize().scale(random.nextDouble() * radius));
                
                level.addParticle(ModParticles.SANDSTORM.get(), false, sparkVec.x, sparkVec.y, sparkVec.z, 0, 0, 0);
            }
            level.playSound(ClientUtil.getClientPlayer(), center.x, center.y, center.z, ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 
                    SoundCategory.AMBIENT, 0.1F, 1.0F);
        }
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
            if (target instanceof LivingEntity) {
                DamageUtil.knockback3d((LivingEntity) target, radius * 0.035F, xRot, yRot);
            }
        }
    }
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {
        super.afterBlockHit(blockRayTraceResult, blockDestroyed);
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (targetType != TargetType.ENTITY) {
            if (!level.isClientSide()) {
                remove();
            } else {
                super.breakProjectile(targetType, hitTarget);
            }
        }
    }
    
    @Override
    public EntitySize getDimensions(Pose pose) {
        EntitySize defaultSize = super.getDimensions(pose);
        return new EntitySize(radius * 1.2F, radius * 1.2F, defaultSize.fixed);
    }
    
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public int ticksLifespan() {
        return duration;
    }

    @Override
    protected float getBaseDamage() {
        return damage;
    }
    
    @Override
    protected float knockbackMultiplier() {
        return  0.1F;
    }
    
    @Override
    protected float getDamageAmount() {
        return damage * Math.max(damageWearOffMultiplier(), 0.5F);
    }
    
    private float damageWearOffMultiplier() {
        float ageRatio = (float) tickCount / (float) duration;
        return Math.min(2 - ageRatio * 2, 1);
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 5.0F;
    }
    
    @Override
    public boolean canBeDeflected(@Nullable Entity context) {
        return false;
    }
    
    @Override
    public boolean canBeEvaded(@Nullable Entity context) {
        return false;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Radius", radius);
        nbt.putFloat("Damage", damage);
        nbt.putInt("Duration", duration);
        nbt.putFloat("Particles", particlesCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        setRadius(nbt.getFloat("Radius"));
        damage = nbt.getFloat("Damage");
        duration = nbt.getInt("Duration");
        particlesCount = nbt.getFloat("Particles");
    }
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(radius);
        buffer.writeVarInt(duration);
        buffer.writeFloat(particlesCount);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        setRadius(additionalData.readFloat());
        setDuration(additionalData.readVarInt());
        particlesCount = additionalData.readFloat();
    }
    
    private static final Vector3d OFFSET = new Vector3d(0, 0.8F, 0);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }
    
    
}
