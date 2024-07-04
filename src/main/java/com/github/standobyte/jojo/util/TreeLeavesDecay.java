package com.github.standobyte.jojo.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public class TreeLeavesDecay {
    public Set<BlockPos> logs = new HashSet<>();
    public List<Set<BlockPos>> leaves = new ArrayList<>();
    public Block logType;
    public Block leavesType;
    public int decayTicks = 0;
    public int decayPerTick = 0;
    
    private static final int RANGE = 16;
    
    
    @Nullable
    public static TreeLeavesDecay startDecay(World world, BlockPos blockPos, int duration, int leavesPerTick) {
        if (!world.isClientSide()) {
            return world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve().map(cap -> {
                TreeLeavesDecay tree = TreeLeavesDecay.createFromLogBlock(blockPos, world);
                if (tree != null && tree.isValid()) {
                    cap.addDecayingTree(tree);
                    tree.updateDecay(duration, leavesPerTick);
                    return tree;
                }
                
                return null;
            }).orElse(null);
        }
        
        return null;
    }
    
    public static boolean isTreeStemBlock(Block block) {
        return BlockTags.LOGS.contains(block) || block == Blocks.MUSHROOM_STEM;
    }
    
    @Nullable
    public static TreeLeavesDecay createFromLogBlock(BlockPos pos, World world) {
        Block block = world.getBlockState(pos).getBlock();
        if (isTreeStemBlock(block)) {
            TreeLeavesDecay tree = new TreeLeavesDecay();
            tree.logType = block;
            CubeBoolArrUtil checkedTable = new CubeBoolArrUtil(RANGE * 2 + 1);
            tree.recAddTreeBlock(pos, new Vector3i(0, 0, 0), world, checkedTable);
            tree.leaves = Lists.reverse(tree.leaves);
            return tree;
        }
        
        return null;
    }
    
    private void recAddTreeBlock(BlockPos originalPos, Vector3i offset, World world, CubeBoolArrUtil checkedTable) {
        int distance = Math.abs(offset.getX()) + Math.abs(offset.getY()) + Math.abs(offset.getZ());
        if (distance > RANGE) {
            return;
        }
        
        int iX = offset.getX() + RANGE;
        int iY = offset.getY() + RANGE;
        int iZ = offset.getZ() + RANGE;
        if (checkedTable.get(iX, iY, iZ)) {
            return;
        }
        checkedTable.set(iX, iY, iZ, true);
        
        boolean validBlock = false;
        BlockPos pos = originalPos.offset(offset);
        Block block = world.getBlockState(pos).getBlock();
        
        if (logType == block) {
            validBlock = true;
            logs.add(pos);
        }
        
        else {
            if (leavesType != null) {
                validBlock = leavesType == block;
            }
            else if (block instanceof LeavesBlock || block instanceof HugeMushroomBlock) {
                leavesType = block;
                validBlock = true;
            }
            if (validBlock) {
                for (int i = leaves.size(); i < distance; ++i) {
                    leaves.add(new HashSet<>());
                }
                Set<BlockPos> leavesOnDist = leaves.get(distance - 1);
                leavesOnDist.add(pos);
            }
        }
        
        if (!validBlock) {
            return;
        }
        
        if (distance < RANGE) {
            for (Direction direction : Direction.values()) {
                Vector3i nextOffset = new Vector3i(
                        offset.getX() + direction.getStepX(), 
                        offset.getY() + direction.getStepY(), 
                        offset.getZ() + direction.getStepZ());
                recAddTreeBlock(originalPos, nextOffset, world, checkedTable);
                if (direction.getAxis() != Direction.Axis.Y) {
                    recAddTreeBlock(originalPos, new Vector3i(
                            offset.getX() + direction.getStepX(), 
                            offset.getY() + direction.getStepY() - 1, 
                            offset.getZ() + direction.getStepZ()), world, checkedTable);
                }
            }
        }
    }
    
    public boolean containsLog(BlockPos pos) {
        return logs.contains(pos);
    }
    
    public boolean isValid() {
        return leavesType != null;
    }
    
    public void updateDecay(int ticks, int leavesPerTick) {
        this.decayPerTick = Math.max(leavesPerTick, this.decayPerTick);
        this.decayTicks = Math.max(ticks, this.decayTicks);
    }
    
    public boolean tick(World world) {
        if (decayTicks <= 0) {
            return true;
        }
        --decayTicks;
        
        int leavesRemoved = 0;
        Iterator<Set<BlockPos>> iter = leaves.iterator();
        while (leavesRemoved < decayPerTick && iter.hasNext()) {
            Set<BlockPos> furthestLeaves = iter.next();
            Iterator<BlockPos> setIter = furthestLeaves.iterator();
            while (leavesRemoved < decayPerTick && setIter.hasNext()) {
                BlockPos pos = setIter.next();
                BlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock() == leavesType) {
                    Block.dropResources(blockState, world, pos);
                    if (world.removeBlock(pos, false)) {
                        ++leavesRemoved;
                    }
                }
                setIter.remove();
            }
            if (furthestLeaves.isEmpty()) {
                iter.remove();
            }
        }
        
        return leaves.isEmpty();
    }
    
    
    
    private static class CubeBoolArrUtil {
        private final long[] data;
        private final int size;
        
        private CubeBoolArrUtil(int size) {
            int bits = size * size * size;
            int arrSize = bits / 64 + (bits % 64 > 0 ? 1 : 0);
            this.data = new long[arrSize];
            this.size = size;
        }
        
        private boolean get(int i, int j, int k) {
            int index = i * size * size + j * size + k;
            return (data[index / 64] & (((long) 1) << (index % 64))) > 0;
        }
        
        private void set(int i, int j, int k, boolean val) {
            int index = i * size * size + j * size + k;
            if (val) {
                data[index / 64] |= ((long) 1) << (index % 64); 
            }
            else {
                data[index / 64] &= ~(((long) 1) << (index % 64));
            }
        }
    }
}
