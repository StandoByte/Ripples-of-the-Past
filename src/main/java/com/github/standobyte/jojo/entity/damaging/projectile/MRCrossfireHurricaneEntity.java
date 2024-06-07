package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.IndirectStandEntityDamageSource;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion.CustomExplosionType;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
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
            burnBlocksTick();
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
    
    private void burnBlocksTick() {
        if (!level.isClientSide() && !small && JojoModUtil.breakingBlocksEnabled(level)) {
            ServerWorld world = (ServerWorld) level;
            LivingEntity owner = getOwner();
            
            AxisAlignedBB fireAABB = getBoundingBox().move(getDeltaMovement()).inflate(0.5);
            BlockPos pos1 = new BlockPos(fireAABB.minX, fireAABB.minY, fireAABB.minZ);
            BlockPos pos2 = new BlockPos(fireAABB.maxX, fireAABB.maxY, fireAABB.maxZ);

            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                    for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockState blockState = level.getBlockState(blockPos);
                        if (JojoModUtil.canEntityDestroy(world, blockPos, level.getBlockState(blockPos), owner)
                                && !MRFlameEntity.meltIceAndSnow(level, blockState, blockPos) && blockState.isFlammable(level, blockPos, Direction.UP)) {
                            blockState.catchFire(level, blockPos, Direction.UP, getOwner());
                            CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, blockState, 
                                    Optional.ofNullable(world.getBlockEntity(blockPos)), Collections.emptyList());
                            level.removeBlock(blockPos, false);
                        }
                    }
                }
            }

            setOnFire(  
                    pos1.getX(), pos1.getY(), pos1.getZ(), 
                    pos2.getX(), pos1.getY(), pos2.getZ(), 
                    Direction.DOWN);
            setOnFire(  
                    pos1.getX(), pos2.getY(), pos1.getZ(), 
                    pos2.getX(), pos2.getY(), pos2.getZ(), 
                    Direction.UP);
            setOnFire(  
                    pos1.getX(), pos1.getY(), pos1.getZ(), 
                    pos2.getX(), pos2.getY(), pos1.getZ(), 
                    Direction.NORTH);
            setOnFire(  
                    pos1.getX(), pos1.getY(), pos2.getZ(), 
                    pos2.getX(), pos2.getY(), pos2.getZ(), 
                    Direction.SOUTH);
            setOnFire(  
                    pos1.getX(), pos1.getY(), pos1.getZ(), 
                    pos1.getX(), pos2.getY(), pos2.getZ(), 
                    Direction.WEST);
            setOnFire(  
                    pos2.getX(), pos1.getY(), pos1.getZ(), 
                    pos2.getX(), pos2.getY(), pos2.getZ(), 
                    Direction.EAST);
        }
    }
    
    private void setOnFire(int x1, int y1, int z1, int x2, int y2, int z2, Direction direction) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (level.isEmptyBlock(blockPos)) {
                        BlockState blockState = level.getBlockState(blockPos);
                        LivingEntity user = StandUtil.getStandUser(getOwner());
                        if (user != null && user.getBoundingBox().intersects(new AxisAlignedBB(blockPos))) {
                            return;
                        }
                        BlockPos blockPosSolid = blockPos.relative(direction);
                        blockState = level.getBlockState(blockPosSolid);
                        if (blockState.getCollisionShape(level, blockPosSolid) != VoxelShapes.empty()) {
                            level.setBlockAndUpdate(blockPos, ModBlocks.MAGICIANS_RED_FIRE.get().getStateForPlacement(level, blockPos));
                        }
                    }
                }
            }
        }
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
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
            CustomExplosion.explode(level, this, dmgSource.setExplosion(), null, 
                    getX(), getY(), getZ(), (small ? 1.0F : 3.0F) * getScale(), 
                    true, Explosion.Mode.NONE, CustomExplosionType.CROSSFIRE_HURRICANE);
        }
    }
    
    
    public static class CrossfireHurricaneExplosion extends CustomExplosion {
        private final MRCrossfireHurricaneEntity sourceProjectile;
        
        public CrossfireHurricaneExplosion(World pLevel, @Nullable Entity pSource, 
                @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
                double pToBlowX, double pToBlowY, double pToBlowZ, 
                float pRadius, boolean pFire, Explosion.Mode pBlockInteraction) {
            super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
            this.sourceProjectile = pSource instanceof MRCrossfireHurricaneEntity ? (MRCrossfireHurricaneEntity) pSource : null;
        }
        
        @Override
        protected void filterEntities(List<Entity> entities) {
            if (sourceProjectile != null) {
                LivingEntity owner = sourceProjectile.getOwner();
                LivingEntity standUser = owner instanceof StandEntity ? ((StandEntity) owner).getUser() : null;
                boolean canAffectStandUser = standUser != null
                        && IStandPower.getStandPowerOptional(standUser).map(stand -> stand.getResolveLevel() < 4).orElse(true);
                Iterator<Entity> it = entities.iterator();
                while (it.hasNext()) {
                    Entity entity = it.next();
                    if (entity.is(owner) || !canAffectStandUser && entity.is(standUser)) {
                        it.remove();
                    }
                }
            }
        }
        
        @Override
        protected void hurtEntity(Entity entity, float damage, double knockback, Vector3d vecToEntityNorm) {
            super.hurtEntity(entity, damage, knockback, vecToEntityNorm);
            
            LivingEntity magiciansRed = sourceProjectile != null ? sourceProjectile.getOwner() : null;
            if (!entity.is(magiciansRed)) {
                DamageUtil.setOnFire(entity, 10, true);
                if (sourceProjectile != null && !level.isClientSide()
                        && sourceProjectile.userStandPower != null && StandUtil.attackingTargetGivesResolve(entity)) {
                    sourceProjectile.userStandPower.addLearningProgressPoints(ModStandsInit.MAGICIANS_RED_CROSSFIRE_HURRICANE.get(), 0.03125F);
                }
            }
        }
        
        @Override
        protected void spawnFire() {
            LivingEntity magiciansRed = sourceProjectile != null ? sourceProjectile.getOwner() : null;
            if (magiciansRed == null || ForgeEventFactory.getMobGriefingEvent(level, magiciansRed)) {
                for (BlockPos pos : getToBlow()) {
                    if (level.isEmptyBlock(pos)) {
                        level.setBlockAndUpdate(pos, ModBlocks.MAGICIANS_RED_FIRE.get().getStateForPlacement(level, pos));
                    }
                    else if (sourceProjectile == null || !sourceProjectile.small) {
                        BlockState blockState = level.getBlockState(pos);
                        if (!MRFlameEntity.meltIceAndSnow(level, blockState, pos) && random.nextFloat() <= 0.25F) {
                            // FIXME (MR)
//                            if (blockState.getMaterial() == Material.STONE && blockState.getDestroySpeed(level, pos) <= 1.5F) {
//                                level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
//                                level.neighborChanged(pos, Blocks.LAVA, pos);
//                            }
                        }
                    }
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
