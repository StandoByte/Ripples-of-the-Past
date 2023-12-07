package com.github.standobyte.jojo.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class MagiciansRedFireBlock extends FireBlock {

    public MagiciansRedFireBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(IBlockReader world, BlockPos blockPos) {
        return super.getStateForPlacement(world, blockPos);
    }
    
//    @Override
//    public void animateTick(BlockState blockState, World world, BlockPos blockPos, Random rand) {
//        if (ClientUtil.canSeeStands()) {
//            super.animateTick(blockState, world, blockPos, rand);
//        }
//    }
    
    @Override
    public BlockState getStateWithAge(IWorld pLevel, BlockPos pPos, int pAge) {
        return getStateForPlacement(pLevel, pPos).setValue(AGE, Integer.valueOf(pAge));
    }
}
