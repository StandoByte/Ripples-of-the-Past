package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonBubbleCutterEntity extends ModdedProjectileEntity { // TODO bubble lenses
    private boolean gliding;
    
    public HamonBubbleCutterEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.HAMON_BUBBLE_CUTTER.get(), shooter, world);
    }

    public HamonBubbleCutterEntity(EntityType<? extends HamonBubbleCutterEntity> type, World world) {
        super(type, world);
    }
    
    public void setGliding(boolean gliding) {
        this.gliding = gliding;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide() && tickCount % 7 == getId() % 7) {
            HamonPowerType.createHamonSparkParticles(level, ClientUtil.getClientPlayer(), getX(), getY(0.5), getZ(), 0.1F);
        }
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        boolean projectileAttack = super.hurtTarget(target, owner);
        boolean hamonAttack = DamageUtil.dealHamonDamage(target, 0.1F, this, owner);
        return projectileAttack || hamonAttack;
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                INonStandPower.getNonStandPowerOptional(owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, 
                                gliding ? ModActions.CAESAR_BUBBLE_CUTTER_GLIDING.get().getEnergyCost(null) / 10F
                                        : ModActions.CAESAR_BUBBLE_CUTTER.get().getEnergyCost(null) / 10F);
                    });
                });
            }
        }
    }
    
    @Override
    protected void onHitBlock(BlockRayTraceResult result) {
        if (gliding && result.getDirection().getAxis() == Axis.Y) {
            Vector3d movementVec = getDeltaMovement();
            Vector3d newVec = new Vector3d(movementVec.x, 0, movementVec.z);
            this.setDeltaMovement(newVec.scale(Math.sqrt(movementVec.lengthSqr() / newVec.lengthSqr())));
        }
        else {
            super.onHitBlock(result);
        }
    }
    
    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public float getBaseDamage() {
        return 1.0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    public int ticksLifespan() {
        return 100;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Gliding", gliding);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        gliding = nbt.getBoolean("Gliding");
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(gliding);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.gliding = additionalData.readBoolean();
    }
}
