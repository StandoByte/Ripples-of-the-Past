package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonTurquoiseBlueOverdriveEntity extends ModdedProjectileEntity {
    private float radius;
    private float damage;
    private float points;
    private float sparksCount;
    private boolean gaveHamonPoints;
    private int duration;

    public HamonTurquoiseBlueOverdriveEntity(World world, LivingEntity entity) {
        super(ModEntityTypes.TURQUOISE_BLUE_OVERDRIVE.get(), entity, world);
    }
    
    public HamonTurquoiseBlueOverdriveEntity setRadius(float radius) {
        this.radius = radius;
        this.sparksCount = radius * radius * 3;
        Vector3d pos = getBoundingBox().getCenter();
        refreshDimensions();
        setBoundingBox(new AxisAlignedBB(pos, pos).inflate(radius));
        return this;
    }
    
    public HamonTurquoiseBlueOverdriveEntity setDamage(float damage) {
        this.damage = damage;
        return this;
    }
    
    public HamonTurquoiseBlueOverdriveEntity setPoints(float points) {
        this.points = points;
        return this;
    }
    
    public HamonTurquoiseBlueOverdriveEntity setDuration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public HamonTurquoiseBlueOverdriveEntity(EntityType<? extends HamonTurquoiseBlueOverdriveEntity> entityType, World world) {
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
            int sparksCount = Math.max((int) (this.sparksCount * damageWearOffMultiplier()), 1);
            for (int i = 0; i < sparksCount; i++) {
                Vector3d sparkVec = center.add(new Vector3d(
                        (random.nextDouble() - 0.5), 
                        (random.nextDouble() - 0.5),
                        (random.nextDouble() - 0.5))
                        .normalize().scale(random.nextDouble() * radius));
                if (level.isWaterAt(new BlockPos(sparkVec))) {
                    level.addParticle(ModParticles.HAMON_SPARK_BLUE.get(), false, sparkVec.x, sparkVec.y, sparkVec.z, 0, 0, 0);
                }
            }
            level.playSound(ClientUtil.getClientPlayer(), center.x, center.y, center.z, ModSounds.HAMON_SPARK.get(), 
                    SoundCategory.AMBIENT, Math.min(0.1F + radius * 0.15F, 1), 1.0F + (random.nextFloat() - 0.5F) * 0.15F);
        }
    }

    @Override
    protected void checkHit() {
        if (!level.isClientSide()) {
            if (!this.isInWaterOrBubble()) {
                remove();
                return;
            }
            level.getEntitiesOfClass(LivingEntity.class, getBoundingBox(), entity -> entity.isInWaterOrBubble() && canHitEntity(entity)).forEach(target -> {
                onHitEntity(new EntityRayTraceResult(target));
            });
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, getDamageAmount(), 
                this, owner, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_BLUE.get()));
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
            if (target.isInWaterOrBubble() && target instanceof LivingEntity) {
                DamageUtil.knockback3d((LivingEntity) target, radius * 0.1F, xRot, yRot);
            }
            if (!gaveHamonPoints) {
                INonStandPower.getNonStandPowerOptional(getOwner()).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        gaveHamonPoints = true;
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, points);
                    });
                });
            }
        }
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (targetType != TargetType.ENTITY) {
            super.breakProjectile(targetType, hitTarget);
        }
    }
    
    @Override
    public EntitySize getDimensions(Pose pose) {
        EntitySize defaultSize = super.getDimensions(pose);
        return new EntitySize(radius * 2, radius * 2, defaultSize.fixed);
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
    protected float getDamageAmount() {
        return damage * damageWearOffMultiplier();
    }
    
    private float damageWearOffMultiplier() {
        float ageRatio = (float) tickCount / (float) duration;
        return Math.min(2 - ageRatio * 2, 1);
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Radius", radius);
        nbt.putBoolean("PointsGiven", gaveHamonPoints);
        nbt.putFloat("Damage", damage);
        nbt.putFloat("Points", points);
        nbt.putInt("Duration", duration);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        setRadius(nbt.getFloat("Radius"));
        gaveHamonPoints = nbt.getBoolean("PointsGiven");
        damage = nbt.getFloat("Damage");
        points = nbt.getFloat("Points");
        duration = nbt.getInt("Duration");
    }
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(radius);
        buffer.writeVarInt(duration);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        setRadius(additionalData.readFloat());
        setDuration(additionalData.readVarInt());
    }
}
