package com.github.standobyte.jojo.util.mc;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class LiquidOnlyRayTraceContext extends RayTraceContext {

    public LiquidOnlyRayTraceContext(Vector3d from, Vector3d to, FluidMode liquid,
            Entity collidingEntity) {
        super(from, to, null, liquid, collidingEntity);
    }
    
    @Override
    public VoxelShape getBlockShape(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        return VoxelShapes.empty();
    }
}
