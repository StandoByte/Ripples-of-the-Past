package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Collections;
import java.util.Optional;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

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
    
    protected MRFlameEntity(EntityType<? extends MRFlameEntity> type, LivingEntity shooter, World world) {
        super(type, shooter, world);
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
    protected float knockbackMultiplier() {
        return 0.1F;
    }
    
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        startingPos = position();
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> super.hurtTarget(entity, owner), 10, true);
    }

    @Override
    protected RayTraceResult[] rayTrace() {
        return new RayTraceResult[] { JojoModUtil.getHitResult(this, this::canHitEntity, RayTraceContext.BlockMode.OUTLINE) };
    }
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {
        if (!level.isClientSide) {
            if (ForgeEventFactory.getMobGriefingEvent(level, getEntity())) {
                BlockPos blockPos = blockRayTraceResult.getBlockPos();
                BlockState blockState = level.getBlockState(blockPos);
                if (!meltIceAndSnow(level, blockState, blockPos) && 
                        blockState.getCollisionShape(level, blockPos) != VoxelShapes.empty()) {
                    blockPos = blockPos.relative(blockRayTraceResult.getDirection());
                    if (level.isEmptyBlock(blockPos)) {
                        level.setBlockAndUpdate(blockPos, ModBlocks.MAGICIANS_RED_FIRE.get().getStateForPlacement(level, blockPos));
                    }
                }
            }
        }
    }
    
    public static boolean meltIceAndSnow(World world, BlockState blockState, BlockPos blockPos) {
        if (world.isClientSide()) return false;
        if (blockState.getMaterial() == Material.SNOW || blockState.getMaterial() == Material.TOP_SNOW 
                || blockState.getMaterial() == Material.ICE || blockState.getMaterial() == Material.ICE_SOLID) {
            if (world.dimensionType().ultraWarm() || !blockState.isCollisionShapeFullBlock(world, blockPos)) {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, blockState, 
                        Optional.ofNullable(world.getBlockEntity(blockPos)), Collections.emptyList());
                world.removeBlock(blockPos, false);
            }
            else {
                world.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
                world.neighborChanged(blockPos, Blocks.WATER, blockPos);
            }
            return true;
        }
        return false;
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (targetType == TargetType.BLOCK) {
            BlockRayTraceResult blockHit = (BlockRayTraceResult) hitTarget;
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            if (!blockState.isCollisionShapeFullBlock(level, blockPos)) return;
        }
        super.breakProjectile(targetType, hitTarget);
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
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean isFiery() {
        return true;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    public int ticksLifespan() {
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
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        boolean hasStartingPos = startingPos != null;
        buffer.writeBoolean(hasStartingPos);
        if (hasStartingPos) {
            buffer.writeDouble(startingPos.x);
            buffer.writeDouble(startingPos.y);
            buffer.writeDouble(startingPos.z);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        if (additionalData.readBoolean()) {
            startingPos = new Vector3d(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        }
        else {
            startingPos = position();
        }
    }
}
