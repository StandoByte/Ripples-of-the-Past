package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanRibEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private float xRotOffset;
    protected float knockback = 0;
    private double yOriginOffset;
    private double xOriginOffset;
    private boolean isBinding = true;

    public PillarmanRibEntity(World world, LivingEntity entity, float angleXZ, float angleYZ, double offsetX, double offsetY) {
        super(ModEntityTypes.PILLARMAN_RIBS.get(), entity, world);
        this.xRotOffset = angleXZ;
        this.yRotOffset = angleYZ;
        this.xOriginOffset = offsetX;
        this.yOriginOffset = offsetY;
    }
    
    public PillarmanRibEntity(EntityType<? extends PillarmanRibEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        super.tick();
    }
    
    @Override
    public float getBaseDamage() {
        return 0.05F;
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return super.hurtTarget(target, owner);
    }
    
    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
            if (isBinding) {
                if (target instanceof LivingEntity) {
                    LivingEntity livingTarget = (LivingEntity) target;
                    if (!JojoModUtil.isTargetBlocking(livingTarget)) {
                        attachToEntity(livingTarget);
                        livingTarget.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), ticksLifespan() - tickCount));
                    }
                }
            }
        }
    }
    
    @Override
    public boolean standDamage() {
        return false;
    }
    
    public void addKnockback(float knockback) {
        this.knockback = knockback;
    }
    
    public boolean isBinding() {
        return isBinding;
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
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
        if (isAttachedToAnEntity()) {
            ticks += 20;
        }
        return ticks;
    }
    
    @Override
    protected float movementSpeed() {
        return 4 / (float) ticksLifespan();
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
        buffer.writeBoolean(isBinding);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.yRotOffset = additionalData.readFloat();
        this.xRotOffset = additionalData.readFloat();
        this.xOriginOffset = additionalData.readDouble();
        this.yOriginOffset = additionalData.readDouble();
        this.isBinding = additionalData.readBoolean();
    }
}
