package com.github.standobyte.jojo.block;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
    
    


    // all the copy-paste below is to change this single method that is private in FireBlock
    private BlockState getStateWithAge(IWorld pLevel, BlockPos pPos, int pAge) {
        return getStateForPlacement(pLevel, pPos).setValue(AGE, Integer.valueOf(pAge));
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return this.canSurvive(pState, pLevel, pCurrentPos) ? this.getStateWithAge(pLevel, pCurrentPos, pState.getValue(AGE)) : Blocks.AIR.defaultBlockState();
    }
    
    @Override
    public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
        pLevel.getBlockTicks().scheduleTick(pPos, this, getFireTickDelay(pLevel.random));
        if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!pState.canSurvive(pLevel, pPos)) {
                pLevel.removeBlock(pPos, false);
            }

            BlockState blockstate = pLevel.getBlockState(pPos.below());
            boolean flag = blockstate.isFireSource(pLevel, pPos, Direction.UP);
            int i = pState.getValue(AGE);
            if (!flag && pLevel.isRaining() && this.isNearRain(pLevel, pPos) && pRand.nextFloat() < 0.2F + (float)i * 0.03F) {
                pLevel.removeBlock(pPos, false);
            } else {
                int j = Math.min(15, i + pRand.nextInt(3) / 2);
                if (i != j) {
                    pState = pState.setValue(AGE, Integer.valueOf(j));
                    pLevel.setBlock(pPos, pState, 4);
                }

                if (!flag) {
                    if (!this.isValidFireLocation(pLevel, pPos)) {
                        BlockPos blockpos = pPos.below();
                        if (!pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, Direction.UP) || i > 3) {
                            pLevel.removeBlock(pPos, false);
                        }

                        return;
                    }

                    if (i == 15 && pRand.nextInt(4) == 0 && !this.canCatchFire(pLevel, pPos.below(), Direction.UP)) {
                        pLevel.removeBlock(pPos, false);
                        return;
                    }
                }

                boolean flag1 = pLevel.isHumidAt(pPos);
                int k = flag1 ? -50 : 0;
                this.tryCatchFire(pLevel, pPos.east(), 300 + k, pRand, i, Direction.WEST);
                this.tryCatchFire(pLevel, pPos.west(), 300 + k, pRand, i, Direction.EAST);
                this.tryCatchFire(pLevel, pPos.below(), 250 + k, pRand, i, Direction.UP);
                this.tryCatchFire(pLevel, pPos.above(), 250 + k, pRand, i, Direction.DOWN);
                this.tryCatchFire(pLevel, pPos.north(), 300 + k, pRand, i, Direction.SOUTH);
                this.tryCatchFire(pLevel, pPos.south(), 300 + k, pRand, i, Direction.NORTH);
                BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

                for(int l = -1; l <= 1; ++l) {
                    for(int i1 = -1; i1 <= 1; ++i1) {
                        for(int j1 = -1; j1 <= 4; ++j1) {
                            if (l != 0 || j1 != 0 || i1 != 0) {
                                int k1 = 100;
                                if (j1 > 1) {
                                    k1 += (j1 - 1) * 100;
                                }

                                blockpos$mutable.setWithOffset(pPos, l, j1, i1);
                                int l1 = this.getFireOdds(pLevel, blockpos$mutable);
                                if (l1 > 0) {
                                    int i2 = (l1 + 40 + pLevel.getDifficulty().getId() * 7) / (i + 30);
                                    if (flag1) {
                                        i2 /= 2;
                                    }

                                    if (i2 > 0 && pRand.nextInt(k1) <= i2 && (!pLevel.isRaining() || !this.isNearRain(pLevel, blockpos$mutable))) {
                                        int j2 = Math.min(15, i + pRand.nextInt(5) / 4);
                                        pLevel.setBlock(blockpos$mutable, this.getStateWithAge(pLevel, blockpos$mutable, j2), 3);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private void tryCatchFire(World pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face) {
        int i = pLevel.getBlockState(pPos).getFlammability(pLevel, pPos, face);
        if (pRandom.nextInt(pChance) < i) {
            BlockState blockstate = pLevel.getBlockState(pPos);
            if (pRandom.nextInt(pAge + 10) < 5 && !pLevel.isRainingAt(pPos)) {
                int j = Math.min(pAge + pRandom.nextInt(5) / 4, 15);
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(pLevel, pPos, blockstate, 
                        Optional.ofNullable(pLevel.getBlockEntity(pPos)), Collections.emptyList());
                pLevel.setBlock(pPos, this.getStateWithAge(pLevel, pPos, j), 3);
            } else {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(pLevel, pPos, blockstate, 
                        Optional.ofNullable(pLevel.getBlockEntity(pPos)), Collections.emptyList());
                pLevel.removeBlock(pPos, false);
            }

            blockstate.catchFire(pLevel, pPos, face, null);
        }

    }

    /**
     * Gets the delay before this block ticks again (without counting random ticks)
     */
    private static int getFireTickDelay(Random pRandom) {
        return 30 + pRandom.nextInt(10);
    }

    private boolean isValidFireLocation(IBlockReader pLevel, BlockPos pPos) {
        for(Direction direction : Direction.values()) {
            if (this.canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    private int getFireOdds(IWorldReader pLevel, BlockPos pPos) {
        if (!pLevel.isEmptyBlock(pPos)) {
            return 0;
        } else {
            int i = 0;

            for(Direction direction : Direction.values()) {
                BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));
                i = Math.max(blockstate.getFireSpreadSpeed(pLevel, pPos.relative(direction), direction.getOpposite()), i);
            }

            return i;
        }
    }
}
