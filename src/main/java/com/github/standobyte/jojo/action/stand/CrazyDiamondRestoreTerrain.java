package com.github.standobyte.jojo.action.stand;

import java.util.Map;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks;
import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks.PrevBlockInfo;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// FIXME ! (restore terrain) remember more block breaking instances
// FIXME !! (restore terrain) blocks overlay
public class CrazyDiamondRestoreTerrain extends StandEntityAction {
    public static final int MANHATTAN_DIST = 8;

    public CrazyDiamondRestoreTerrain(StandEntityAction.Builder builder) {
        super(builder);
    }

    // FIXME !! (restore terrain) consume items
    // FIXME !! (restore terrain) limit the blocks count
    // FIXME !! (restore terrain) particles
    // FIXME ! (restore terrain) CD restoration sound
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            CrazyDiamondRestorableBlocks blocks = CrazyDiamondRestorableBlocks.getRestorableBlocksEffect(userPower, world);
            PlayerEntity player = (PlayerEntity) userPower.getUser();
            Map<BlockPos, PrevBlockInfo> map = blocks.getBlocksAround(player.blockPosition(), MANHATTAN_DIST)
                    .filter(entry -> !player.getBoundingBox().intersects(new AxisAlignedBB(entry.getKey())))
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
            map.entrySet().forEach(block -> {
                world.setBlockAndUpdate(block.getKey(), block.getValue().state);
                blocks.removeBlock(world, block.getKey());
            });
        }
    }


}
