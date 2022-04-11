package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class MRFlameEntity extends ModdedProjectileEntity {
    private Vector3d startingPos = null;
    
    public MRFlameEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.MR_FLAME.get(), shooter, world);
    }

    public MRFlameEntity(EntityType<? extends MRFlameEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return 1.0F;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> super.hurtTarget(entity, owner), 5, true);
    }

    @Override
    protected RayTraceResult rayTrace() {
        return JojoModUtil.getHitResult(this, this::canHitEntity, RayTraceContext.BlockMode.OUTLINE);
    }
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {
        if (!level.isClientSide) {
            if (ForgeEventFactory.getMobGriefingEvent(level, getEntity())) {
                BlockPos blockPos = blockRayTraceResult.getBlockPos();
                BlockState blockState = level.getBlockState(blockPos);
                if (blockState.getMaterial() == Material.SNOW || blockState.getMaterial() == Material.TOP_SNOW 
                        || blockState.getMaterial() == Material.ICE || blockState.getMaterial() == Material.ICE_SOLID) {
                    if (level.dimensionType().ultraWarm() || !blockState.isCollisionShapeFullBlock(level, blockPos)) {
                        level.removeBlock(blockPos, false);
                    } 
                    else {
                        level.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
                        level.neighborChanged(blockPos, Blocks.WATER, blockPos);
                    }
                }
                else if (blockState.getCollisionShape(level, blockPos) != VoxelShapes.empty()) {
                    blockPos = blockPos.relative(blockRayTraceResult.getDirection());
                    if (level.isEmptyBlock(blockPos)) {
                        level.setBlockAndUpdate(blockPos, AbstractFireBlock.getState(level, blockPos));
                    }
                }
            }
        }
    }

    @Override
    protected boolean canBreakBlock(BlockPos blockPos, BlockState blockState) {
        return super.canBreakBlock(blockPos, blockState) && !(blockState.getBlock() instanceof AbstractFireBlock);
    }
    
    @Override
    protected DamageSource getDamageSource(LivingEntity owner) {
        return super.getDamageSource(owner).setIsFire();
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
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    protected int ticksLifespan() {
        return 8;
    }
    
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return Vector3d.ZERO;
    }
    
    private static final Vector3d OFFSET_XROT = new Vector3d(0, 0.2, 0.0);
    @Override
    protected Vector3d getXRotOffset() {
        return OFFSET_XROT;
    }
    
    public Vector3d getStartingPos() {
        return startingPos;
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        startingPos = position();
    }
}
