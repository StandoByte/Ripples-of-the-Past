package com.github.standobyte.jojo.entity;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class RoadRollerEntity extends Entity {
    private static final DataParameter<Float> HEALTH = EntityDataManager.defineId(RoadRollerEntity.class, DataSerializers.FLOAT);
    private static final float MAX_HEALTH = 400F;
    private int explosionTimeStamp = -1;
    private int ticksInAir = 0;

    public RoadRollerEntity(World world) {
        this(ModEntityTypes.ROAD_ROLLER.get(), world);
    }

    public RoadRollerEntity(EntityType<RoadRollerEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
       return this.getBbHeight() * 0.95D + Math.abs(xRot) / 90D;
    }

    @Override
    public void push(Entity entity) {}
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    @Override
    public boolean canUpdate() {
        return super.canUpdate() || isVehicle() && getPassengers().get(0).canUpdate();
    }

    @Override
    public void tick() {
        super.tick();
        if (!super.canUpdate()) {
            tickCount--;
        }
        Vector3d movement = getDeltaMovement();
        if (!isNoGravity()) {
            setDeltaMovement(movement.add(-movement.x, -0.04D, -movement.z));
        }
        boolean wasOnGround = onGround;
        move(MoverType.SELF, getDeltaMovement());
        if (level.isClientSide()) {
            if (!wasOnGround) {
                ticksInAir++;
                if (onGround) {
                    level.playSound(ClientUtil.getClientPlayer(), getX(), getY(), getZ(), ModSounds.ROAD_ROLLER_LAND.get(), 
                            getSoundSource(), (float) ticksInAir * 0.025F, 1.0F);
                    ticksInAir = 0;
                }
            }
        }
        if (!level.isClientSide()) {
            float damage = (float) -movement.y * 20F;
            if (damage > 0) {
                AxisAlignedBB aabb = getBoundingBox().contract(0, getBbHeight() * 3 / 4, 0);
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, EntityPredicates.LIVING_ENTITY_STILL_ALIVE);
                for (LivingEntity entity : entities) {
                    if (!this.is(entity.getVehicle())) {
                        entity.hurt(ModDamageSources.roadRollerDamage(this), damage);
                    }
                }
            }
        }
        if (onGround) {
            setDeltaMovement(movement.x, 0, movement.z);
            if (xRot > 0) {
                xRot = Math.max(xRot - 6, 0);
            }
            else {
                xRot = Math.min(xRot + 6, 0);
            }
        }
        int ticksExpl = getTicksBeforeExplosion();
        if (ticksExpl == 0) {
            remove();
            if (!level.isClientSide()) {
                explode();
            }
        }
    }

    private void explode() {
        level.explode(this, getX(), getY(0.0625D), getZ(), 4.0F, Explosion.Mode.NONE);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    
    @Override
    public boolean isPickable() {
        return true;
    }

    private static final Vector3d UPWARDS_VECTOR = new Vector3d(0.0D, 1.0D, 0.0D);
    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        } else {
            Vector3d dmgPos = dmgSource.getSourcePosition();
            if (dmgPos != null) {
                Vector3d dmgVec = dmgPos.vectorTo(position()).normalize();
                double cos = dmgVec.dot(UPWARDS_VECTOR);
                Vector3d movement = getDeltaMovement();
                setDeltaMovement(movement.x, Math.min(movement.y + cos * amount * 0.04D, 0), movement.z);
            }
            if (!level.isClientSide()) {
                setHealth(getHealth() - amount);
                markHurt();
                level.playSound(null, getX(), getY(), getZ(), ModSounds.ROAD_ROLLER_HIT.get(), 
                        getSoundSource(), amount * 0.25F, 1.0F + (random.nextFloat() - 0.5F) * 0.3F);
            }
            return true;
        }
    }

    public float getHealth() {
        return entityData.get(HEALTH);
    }

    public void setHealth(float health) {
        entityData.set(HEALTH, MathHelper.clamp(health, 0.0F, MAX_HEALTH));
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> dataParameter) {
        super.onSyncedDataUpdated(dataParameter);
        if (HEALTH.equals(dataParameter) && getHealth() == 0.0F) {
            explosionTimeStamp = tickCount;
        }
    }
    
    public int getTicksBeforeExplosion() {
        return explosionTimeStamp == -1 ? -1 : Math.max(getTicksToExplode() + explosionTimeStamp - tickCount , 0);
    }
    
    private int getTicksToExplode() {
        return 60;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(HEALTH, MAX_HEALTH);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        if (nbt.contains("Health")) {
            setHealth(nbt.getFloat("Health"));
        }
        if (nbt.contains("ExplosionTime")) {
            explosionTimeStamp = nbt.getInt("ExplosionTime");
        }
        tickCount = nbt.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putFloat("Health", getHealth());
        nbt.putInt("Age", tickCount);
        nbt.putInt("ExplosionTime", explosionTimeStamp);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
