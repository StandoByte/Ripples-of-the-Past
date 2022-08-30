package com.github.standobyte.jojo.action.stand;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;

public class CrazyDiamondRestoreTerrain extends StandEntityAction {
    public static final int RESTORATION_RANGE = 9;
    private static final int BLOCKS_PER_TICK = 3;

    public CrazyDiamondRestoreTerrain(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    // FIXME !!! (restore terrain) particles
    // FIXME ! (restore terrain) CD restoration sound
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            IInventory userInventory;
            boolean creative;
            if (user instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) user;
                userInventory = player.inventory;
                creative = player.abilities.instabuild;
            }
            else {
                userInventory = null;
                creative = false;
            }
            Entity cameraEntity = restorationCenterEntity(user, userPower);
            List<ItemEntity> itemsAround = world.getEntitiesOfClass(ItemEntity.class, cameraEntity.getBoundingBox().inflate(RESTORATION_RANGE * 2));
            Set<BlockPos> blocksPlaced = new HashSet<>();
            Set<BlockPos> blocksToForget = new HashSet<>();
            Vector3i eyePos = eyePos(cameraEntity);
            Vector3d lookVec = cameraEntity.getLookAngle();
            Vector3d eyePosD = cameraEntity.getEyePosition(1.0F);
            
            getBlocksInRange(world, user, eyePos, RESTORATION_RANGE, block -> blockPosSelectedForRestoration(block, cameraEntity, lookVec, eyePosD, eyePos))
            .sorted((bl1, bl2) -> bl1.pos.distManhattan(eyePos) - bl2.pos.distManhattan(eyePos))
            .anyMatch(block -> {
                if (tryReplaceBlock(world, block.pos, block.state, blocksPlaced, creative, block.drops, userInventory, itemsAround)) {
                    blocksToForget.add(block.pos);
                }
                return blocksPlaced.size() >= BLOCKS_PER_TICK;
            });
            
            forgetBrokenBlocks(world, blocksToForget);
        }
    }
    
    
    // FIXME !!! (restore terrain) tile entities (shulker (test for both dupes and item disappearance))
    private static boolean tryReplaceBlock(World world, BlockPos blockPos, BlockState blockState, Set<BlockPos> placedBlocks, 
            boolean isCreative, List<ItemStack> restorationCost, @Nullable IInventory userInventory, List<ItemEntity> itemEntities) {
        BlockState currentBlockState = world.getBlockState(blockPos);
        if (currentBlockState.isAir(world, blockPos)) {
            if (!(isCreative || consumeNeededItems(restorationCost, userInventory, itemEntities))) {
                return false;
            }
            world.setBlockAndUpdate(blockPos, blockState);
            placedBlocks.add(blockPos);
            return true;
        }
        else {
            return true;
        }
    }
    
    private static boolean consumeNeededItems(List<ItemStack> restorationCost, @Nullable IInventory userInventory, List<ItemEntity> itemEntities) {
        Map<ItemStack, Integer> neededItemsGathered = new HashMap<>();
        if (!restorationCost.stream().allMatch(costStack -> {
            if (costStack.isEmpty()) {
                return true;
            }
            int gatheredItemsCount = 0;
            
            for (ItemEntity itemEntity : itemEntities) {
                ItemStack lyingStack = itemEntity.getItem();
                gatheredItemsCount += gatherMatchingItems(costStack, lyingStack, gatheredItemsCount, neededItemsGathered);
                if (gatheredItemsCount >= costStack.getCount()) return true;
            }
            
            if (userInventory != null) {
                int size = userInventory.getContainerSize();
                for (int i = 0; i < size; i++) {
                    ItemStack inventoryItem = userInventory.getItem(i);
                    if (inventoryItem != null) {
                        gatheredItemsCount += gatherMatchingItems(costStack, inventoryItem, gatheredItemsCount, neededItemsGathered);
                        if (gatheredItemsCount >= costStack.getCount()) return true;
                    }
                }
                
            }
            
            return false;
            
        })) {
            return false;
        }
        else {
            neededItemsGathered.forEach((stack, cost) -> {
                stack.shrink(cost);
            });
            return true;
        }
    }
    
    private static int gatherMatchingItems(ItemStack neededItem, ItemStack item, int alreadyGathered, Map<ItemStack, Integer> gatheringMap) {
        if (!item.isEmpty() && item.getItem() == neededItem.getItem() && ItemStack.tagMatches(item, neededItem)) {
            int count = Math.min(item.getCount(), neededItem.getCount() - alreadyGathered);
            if (count > 0) {
                gatheringMap.put(item, count);
                return count;
            }
        }
        return 0;
    }
    
    

    // FIXME !!! (restore terrain) add particles & sounds (the task is server only)
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
    
    
    
    // FIXME !!! (restore terrain) multi-blocks
    public static void rememberBrokenBlock(World world, BlockPos pos, BlockState state, Optional<TileEntity> tileEntity, List<ItemStack> drops) {
        IChunk chunk = world.getChunk(pos);
        if (chunk instanceof Chunk) {
            ((Chunk) chunk).getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.saveBrokenBlock(pos, state, tileEntity, drops);
            });
        }
    }
    
    public static void rememberBrokenBlockCreative(World world, BlockPos pos, BlockState state) {
        // FIXME !!! (restore terrain) remember blocks broken in creative
        IChunk chunk = world.getChunk(pos);
        if (chunk instanceof Chunk) {
            ((Chunk) chunk).getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> {
//                cap.saveBrokenBlock(pos, state, tileEntity, drops);
            });
        }
    }
    
    private static void forgetBrokenBlocks(World world, Collection<BlockPos> posCollection) {
        posCollection.stream()
        .map(pos -> world.getChunk(pos))
        .distinct()
        .forEach(ichunk -> {
            if (ichunk instanceof Chunk) {
                ((Chunk) ichunk).getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> {
                    posCollection.forEach(pos -> cap.removeBrokenBlock(pos));
                });
            }
        });
    }
    

    
    public static Stream<PrevBlockInfo> getBlocksInRange(World world, LivingEntity user, Vector3i center, int blockRange, Predicate<PrevBlockInfo> filter) {
        int chunkXMin = center.getX() - blockRange >> 4;
        int chunkXMax = center.getX() + blockRange >> 4;
        int chunkZMin = center.getZ() - blockRange >> 4;
        int chunkZMax = center.getZ() + blockRange >> 4;
        Stream.Builder<Chunk> builder = Stream.builder();
        for (int x = chunkXMin; x <= chunkXMax; x++) {
            for (int z = chunkZMin; z <= chunkZMax; z++) {
                Chunk chunk = world.getChunk(x, z);
                if (chunk != null) {
                    builder.add(chunk);
                }
            }
        }
        return builder.build().flatMap(chunk -> {
            return chunk.getCapability(ChunkCapProvider.CAPABILITY).map(cap -> {
                return cap.getBrokenBlocks()
                .filter(block -> block.pos.distManhattan(center) <= blockRange && !user.getBoundingBox().intersects(new AxisAlignedBB(block.pos))
                && filter.test(block));
            }).orElse(Stream.empty());
        });
    }
    
    public static Entity restorationCenterEntity(LivingEntity user, IStandPower power) {
        if (power.getStandManifestation() instanceof StandEntity) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            if (stand.isManuallyControlled()) {
                return stand;
            }
        }
        return user;
    }
    
    public static Vector3i eyePos(Entity entity) {
        Vector3d pos = entity.getEyePosition(1.0F);
        return new Vector3i((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z));
    }
    
    public static boolean blockPosSelectedForRestoration(PrevBlockInfo block, Entity cameraEntity, Vector3d entityLookVec, Vector3d entityEyePos, Vector3i restorationCenter) {
        return block.pos.distManhattan(restorationCenter) <= CrazyDiamondRestoreTerrain.RESTORATION_RANGE
                && entityLookVec.dot(Vector3d.atCenterOf(block.pos).subtract(entityEyePos).normalize()) >= 0.5
                ;
    }
}
