package com.github.standobyte.jojo.entity.damaging;

import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class LightBeamEntity extends DamagingEntity {
    protected RayTraceResult target;
    protected float length;
    protected float damage;

    public LightBeamEntity(EntityType<? extends LightBeamEntity> entityType, LivingEntity shooter, World world) {
        super(entityType, shooter, world);
    }

    public LightBeamEntity(EntityType<? extends LightBeamEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public void shoot(float damage, float length) {
        this.damage = damage;
        this.length = length;
        LivingEntity shooter = getOwner();
        if (shooter != null) {
            target = rayTrace()[0];
            if (target.getType() != RayTraceResult.Type.MISS) {
                length = MathHelper.sqrt(shooter.distanceToSqr(target.getLocation()));
            }
        }
    }

    @Override
    public RayTraceResult[] rayTrace() {
        return new RayTraceResult[] { JojoModUtil.rayTrace(this, length, e -> e != getOwner()) };
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            remove();
        }
    }
    
    @Override
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult) {
        if (!level.isClientSide()) {
            Entity target = entityRayTraceResult.getEntity();
            target.setSecondsOnFire((int) damage / 2);
            DamageUtil.dealUltravioletDamage(target, damage, this, getOwner(), false);
        }
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult blockRayTraceResult) {
        if (!level.isClientSide()) {
            BlockPos blockPos = blockRayTraceResult.getBlockPos().relative(blockRayTraceResult.getDirection());
            if (level.isEmptyBlock(blockPos)) {
                level.setBlockAndUpdate(blockPos, AbstractFireBlock.getState(level, blockPos));
            }
        }
    }
    
    @Override
    public float getBaseDamage() {
        return damage;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 2.5F;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }
    
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return getBoundingBox().expandTowards(getEndPoint().subtract(position()));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return super.shouldRenderAtSqrDistance(distance - length * length);
    }
    
    public Vector3d getEndPoint() {
        return position().add(Vector3d.directionFromRotation(xRot, yRot).scale(length));
    }
    
    public float getLength() {
        return length;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Length", length);
        nbt.putFloat("Damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        damage = nbt.getFloat("Damage");
        length = nbt.getFloat("Length");
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(length);
        buffer.writeFloat(damage);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        length = additionalData.readFloat();
        damage = additionalData.readFloat();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public int ticksLifespan() {
        return 1;
    }

}
