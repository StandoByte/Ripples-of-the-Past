package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanVeinEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private float xRotOffset;
    protected float knockback = 0;
    private double yOriginOffset;
    private double xOriginOffset;

    public PillarmanVeinEntity(World world, LivingEntity entity, float angleXZ, float angleYZ, double offsetX, double offsetY) {
        super(ModEntityTypes.PILLARMAN_VEINS.get(), entity, world);
        this.xRotOffset = angleXZ;
        this.yRotOffset = angleYZ;
        this.xOriginOffset = offsetX;
        this.yOriginOffset = offsetY;
    }
    
    public PillarmanVeinEntity(EntityType<? extends PillarmanVeinEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            Vector3d center = getBoundingBox().getCenter();
            for (int i = 0; i < 1; i++) {
                Vector3d sparkVec = center.add(new Vector3d(
                        (random.nextDouble() - 0.5), 
                        (random.nextDouble() - 0.5),
                        (random.nextDouble() - 0.5))
                        .normalize().scale(random.nextDouble() * 2));
                level.addParticle(ModParticles.BLOOD.get(), false, sparkVec.x, sparkVec.y, sparkVec.z, 0, -1, 0);
            }
        }
    }
    
    @Override
    public float getBaseDamage() {
        return 1F;
    }
    
    public void addKnockback(float knockback) {
        this.knockback = knockback;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> super.hurtTarget(entity, owner), 10, true);
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }
    
    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
                if (knockback > 0 && target instanceof LivingEntity) {
                    DamageUtil.knockback((LivingEntity) target, knockback, yRot);
                }
                setIsRetracting(true);
            }
    }
    
    @Override
    protected float knockbackMultiplier() {
        return 0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }

    @Override
    public int ticksLifespan() {
        int ticks = super.ticksLifespan();
        return ticks;
    }
    
    @Override
    protected float movementSpeed() {
        return 16 / (float) ticksLifespan();
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }
    
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return new Vector3d(xOriginOffset, yOriginOffset, 0);
    }

    @Override
    protected Vector3d originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot + xRotOffset, distance);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(yRotOffset);
        buffer.writeFloat(xRotOffset);
        buffer.writeDouble(xOriginOffset);
        buffer.writeDouble(yOriginOffset);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.yRotOffset = additionalData.readFloat();
        this.xRotOffset = additionalData.readFloat();
        this.xOriginOffset = additionalData.readDouble();
        this.yOriginOffset = additionalData.readDouble();
    }
}
