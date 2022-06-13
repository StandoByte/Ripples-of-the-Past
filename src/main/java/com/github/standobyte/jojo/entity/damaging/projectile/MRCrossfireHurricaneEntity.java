package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.damage.IndirectStandEntityDamageSource;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class MRCrossfireHurricaneEntity extends ModdedProjectileEntity {
    private boolean small;
    private float scale = 1F;
    private Vector3d targetPos;
    @Nullable
    private IStandPower userStandPower;
    
    public MRCrossfireHurricaneEntity(boolean small, LivingEntity shooter, World world, IStandPower standPower) {
        super(small ? ModEntityTypes.MR_CROSSFIRE_HURRICANE_SPECIAL.get() : ModEntityTypes.MR_CROSSFIRE_HURRICANE.get(), shooter, world);
        this.small = small;
        userStandPower = standPower;
    }

    public MRCrossfireHurricaneEntity(EntityType<? extends MRCrossfireHurricaneEntity> type, World world) {
        super(type, world);
    }
    
    public void setSpecial(Vector3d targetPos) {
        this.targetPos = targetPos;
    }
    
    public void setScale(float scale) {
    	this.scale = scale;
    }
    
    public float getScale() {
    	return scale;
    }

    @Override
    public EntitySize getDimensions(Pose pose) {
    	return super.getDimensions(pose).scale(scale);
    }

    @Override
    protected void moveProjectile() {
        super.moveProjectile();
        if (targetPos != null) {
            double velocitySqr = getDeltaMovement().lengthSqr();
            if (velocitySqr > 0) {
                Vector3d targetVec = targetPos.subtract(position());
                double targetDistSqr = targetVec.lengthSqr();
                if (velocitySqr < targetDistSqr) {
                    Vector3d vec = getDeltaMovement().scale(targetDistSqr / velocitySqr);
                    setDeltaMovement(vec.add(targetVec).normalize().scale(Math.sqrt(velocitySqr)));
                }
                else if (!level.isClientSide()) {
                    explode();
                }
            }
        }
    }
    
    @Override
    public boolean standDamage() {
        return true;
    }

    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
        }
        else {
            super.tick();
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerWorld) level);
        }
    }
    
    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean isFiery() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return (small ? 2.0F : 6.0F) * scale;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 5.0F;
    }
    
    @Override
	public int ticksLifespan() {
        return 100;
    }
    
    @Override
    protected DamageSource getDamageSource(LivingEntity owner) {
        return super.getDamageSource(owner).setIsFire();
    }
    
    @Override
    public boolean isInvulnerableTo(DamageSource dmgSource) {
        return dmgSource.isExplosion() || super.isInvulnerableTo(dmgSource);
    }
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean brokenBlock) {
        explode();
    }
    
    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        explode();
    }
    
    private void explode() {
        if (!level.isClientSide) {
            StandEntityDamageSource dmgSource = new IndirectStandEntityDamageSource("explosion.stand", this, getOwner());
            if (small) {
                dmgSource.setBypassInvulTicksInEvent();
            }
            level.explode(this, dmgSource.setExplosion(), null, getX(), getY(), getZ(), (small ? 1.0F : 3.0F) * getScale(), false, Explosion.Mode.NONE);
        }
    }
    
    public void explosionFilterEntities(List<Entity> inExplosion) {
        LivingEntity owner = getOwner();
        LivingEntity standUser = getOwner() instanceof StandEntity ? ((StandEntity) owner).getUser() : null;
        boolean canAffectStandUser = standUser != null
                && IStandPower.getStandPowerOptional(standUser).map(stand -> stand.getResolveLevel() < 4).orElse(true);
        Iterator<Entity> it = inExplosion.iterator();
        while (it.hasNext()) {
            Entity entity = it.next();
            if (entity.is(owner) || !canAffectStandUser && entity.is(standUser)) {
                it.remove();
            }
        }
    }
    
    public void onExplode(List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
        LivingEntity magiciansRed = getOwner();
        for (Entity entity : affectedEntities) {
            if (!entity.is(magiciansRed)) {
                DamageUtil.setOnFire(entity, 10, true);
                if (!level.isClientSide() && userStandPower != null && StandUtil.worthyTarget(entity)) {
                    userStandPower.addLearningProgressPoints(ModActions.MAGICIANS_RED_CROSSFIRE_HURRICANE.get(), 0.03125F);
                }
            }
        }
        if (magiciansRed != null && ForgeEventFactory.getMobGriefingEvent(level, magiciansRed)) {
            for (BlockPos pos : affectedBlocks) {
                if (level.isEmptyBlock(pos)) {
                    level.setBlockAndUpdate(pos, AbstractFireBlock.getState(level, pos));
                }
            }
        }
    }
    
    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(targetPos != null);
        if (targetPos != null) {
            buffer.writeDouble(targetPos.x);
            buffer.writeDouble(targetPos.y);
            buffer.writeDouble(targetPos.z);
        }
        buffer.writeFloat(scale);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        if (additionalData.readBoolean()) {
            targetPos = new Vector3d(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        }
        scale = additionalData.readFloat();
    }
}
