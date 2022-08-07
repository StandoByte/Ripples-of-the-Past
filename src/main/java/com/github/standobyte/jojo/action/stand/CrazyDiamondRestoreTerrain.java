package com.github.standobyte.jojo.action.stand;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks;
import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks.PrevBlockInfo;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

// FIXME ! (restore terrain) remember more block breaking instances
// FIXME !! (restore terrain) blocks overlay
public class CrazyDiamondRestoreTerrain extends StandEntityAction {
    public static final int MANHATTAN_DIST = 8;

    public CrazyDiamondRestoreTerrain(StandEntityAction.Builder builder) {
        super(builder);
    }

    // FIXME ! (restore terrain) particles
    // FIXME ! (restore terrain) CD restoration sound
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            CrazyDiamondRestorableBlocks blocks = CrazyDiamondRestorableBlocks.getRestorableBlocksEffect(userPower, world);
            PlayerEntity player = (PlayerEntity) userPower.getUser();
            Map<BlockPos, PrevBlockInfo> map = blocks.getBlocksAround(player.blockPosition(), MANHATTAN_DIST)
                    .filter(entry -> !player.getBoundingBox().intersects(new AxisAlignedBB(entry.getKey())))
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
            List<ItemEntity> itemsAround = world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(MANHATTAN_DIST));
            int blocksToPlace = 4;
            int blocksPlaced = 0;
            Set<BlockPos> placed = new HashSet<>();
            for (Map.Entry<BlockPos, PrevBlockInfo> blockEntry : map.entrySet()) {
                BlockPos blockPos = blockEntry.getKey();
                boolean placedBlock = false;

                // FIXME !!!! that thing
                if (player.abilities.instabuild) {
                    if (!world.isEmptyBlock(blockPos)) {
                        world.destroyBlock(blockPos, true);
                    }
                    world.setBlockAndUpdate(blockPos, blockEntry.getValue().state);
                    placed.add(blockPos);
                    ++blocksPlaced;
                    placedBlock = true;
                }
                
                ItemStack itemNeeded = blockEntry.getValue().stack;

                if (!placedBlock && !itemsAround.isEmpty()) {
                    Iterator<ItemEntity> it = itemsAround.iterator();
                    while (it.hasNext()) {
                        ItemEntity lyingItem = it.next();
                        ItemStack lyingItemStack = lyingItem.getItem();
                        if (consumeFromItemStack(lyingItemStack, itemNeeded)) {
                            if (lyingItemStack.isEmpty()) {
                                lyingItem.remove();
                                it.remove();
                            }
                            if (!world.isEmptyBlock(blockPos)) {
                                world.destroyBlock(blockPos, true);
                            }
                            world.setBlockAndUpdate(blockPos, blockEntry.getValue().state);
                            placed.add(blockPos);
                            ++blocksPlaced;
                            placedBlock = true;
                            break;
                        }
                    }
                }

                if (!placedBlock) {
                    for (int slot = 0; slot < player.inventory.getContainerSize(); ++slot) {
                        ItemStack inventoryItem = player.inventory.getItem(slot);
                        if (consumeFromItemStack(inventoryItem, itemNeeded)) {
                            if (!world.isEmptyBlock(blockPos)) {
                                world.destroyBlock(blockPos, true);
                            }
                            world.setBlockAndUpdate(blockPos, blockEntry.getValue().state);
                            placed.add(blockPos);
                            ++blocksPlaced;
                            placedBlock = true;
                            break;
                        }
                    }
                }
                
                if (placedBlock && blocksPlaced >= blocksToPlace) {
                    break;
                }
            }
            placed.forEach(pos -> {
                blocks.removeBlock(world, pos);
            });
        }
    }
    
    private boolean consumeFromItemStack(ItemStack itemStack, ItemStack itemNeeded) {
        if (ItemStack.isSame(itemStack, itemNeeded)) {
            itemStack.shrink(1);
            return true;
        }
        return false;
    }

    public static void addParticlesAroundBlock(World world, BlockPos blockPos, Random random) {
        Vector3d posLLCorner = Vector3d.atLowerCornerOf(blockPos).subtract(0.25, 0.25, 0.25);
        for (int i = 0; i < 24; i++) {
            world.addParticle(ModParticles.CD_RESTORATION.get(), 
                    posLLCorner.x + random.nextDouble() * 1.5, 
                    posLLCorner.y + random.nextDouble() * 1.5, 
                    posLLCorner.z + random.nextDouble() * 1.5, 
                    0, 0, 0);
        }
    }

}
