package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.item.SatiporojaScarfItem;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SatiporojaScarfEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private HandSide side;
    private boolean gaveHamonPoints;
    
    public SatiporojaScarfEntity(World world, LivingEntity entity, HandSide side) {
        super(ModEntityTypes.SATIPOROJA_SCARF.get(), entity, world);
        this.side = side;
        initYRotOffset();
    }

    public SatiporojaScarfEntity(EntityType<? extends SatiporojaScarfEntity> entityType, World world) {
        super(entityType, world);
    }
    
    private void initYRotOffset() {
        yRotOffset = side == HandSide.RIGHT ? -67.5F: 67.5F;
    }
    
    @Override
    protected float updateDistance() {
        if (side == HandSide.RIGHT) {
            if (yRotOffset < 67.5F) {
                yRotOffset = Math.min(yRotOffset + 135F / ticksLifespan(), 67.5F);
            }
        }
        else {
            if (yRotOffset > -67.5F) {
                yRotOffset = Math.max(yRotOffset - 135F / ticksLifespan(), -67.5F);
            }
        }
        return super.updateDistance();
    }
    
    @Override
    protected float movementSpeed() {
        return 1.5F;
    }

    @Override
    public int ticksLifespan() {
        return 10;
    }

    @Override
    protected Vector3d originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot, distance);
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, 0.6F, this, owner);
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt && !gaveHamonPoints) {
            INonStandPower.getNonStandPowerOptional(getOwner()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    gaveHamonPoints = true;
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, SatiporojaScarfItem.SCARF_SWING_ENERGY_COST);
                });
            });
        }
    }

    @Override
    public float getBaseDamage() {
        return 0;
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
        nbt.putBoolean("LeftArm", side == HandSide.LEFT);
        nbt.putBoolean("PointsGiven", gaveHamonPoints);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        side = nbt.getBoolean("LeftArm") ? HandSide.LEFT : HandSide.RIGHT;
        gaveHamonPoints = nbt.getBoolean("PointsGiven");
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(side == HandSide.LEFT);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        side = additionalData.readBoolean() ? HandSide.LEFT : HandSide.RIGHT;
        initYRotOffset();
    }
}
