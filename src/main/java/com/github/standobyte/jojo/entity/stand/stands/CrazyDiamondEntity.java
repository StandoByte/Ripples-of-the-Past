package com.github.standobyte.jojo.entity.stand.stands;

import java.util.List;

import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CrazyDiamondEntity extends StandEntity {
    
    public CrazyDiamondEntity(StandEntityType<CrazyDiamondEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean breakBlock(BlockPos blockPos, BlockState blockState, boolean canDropItems) {
        if (!level.isClientSide()) {
            List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld) level, blockPos, blockState.hasTileEntity() ? level.getBlockEntity(blockPos) : null);
            if (super.breakBlock(blockPos, blockState, canDropItems)) {
                CrazyDiamondRestorableBlocks.getRestorableBlocksEffect(getUserPower(), level).addBlock(level, blockPos, blockState, drops, true);
                return true;
            }
            return false;
        }
        return super.breakBlock(blockPos, blockState, canDropItems);
    }
}
