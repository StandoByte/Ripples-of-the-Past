package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.CDBlocksRestoredPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;

public class CrazyDiamondRestoreTerrain extends StandEntityAction {

    public CrazyDiamondRestoreTerrain(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        Entity cameraEntity = restorationCenterEntity(user, power);
        Vector3i eyePosI = eyePos(cameraEntity);
        boolean hasResolveEffect = user.hasEffect(ModStatusEffects.RESOLVE.get());
        boolean onlyAimedAt = user.isShiftKeyDown();
        if (getBlocksInRange(user.level, user, eyePosI, restorationDistManhattan(hasResolveEffect), 
                block -> blockPosSelectedForRestoration(block, cameraEntity, cameraEntity.getLookAngle(), 
                        cameraEntity.getEyePosition(1.0F), eyePosI, hasResolveEffect, onlyAimedAt)).count() == 0) {
            return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    // TODO try to mitigate the fps drops when lots of blocks are restored simultaneously
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            IInventory userInventory;
            boolean creative;
            PlayerEntity playerUser = user instanceof PlayerEntity ? (PlayerEntity) user : null;
            if (playerUser != null) {
                userInventory = playerUser.inventory;
                creative = playerUser.abilities.instabuild;
            }
            else {
                userInventory = null;
                creative = false;
            }
            Entity cameraEntity = restorationCenterEntity(user, userPower);
            boolean resolveEffect = user.hasEffect(ModStatusEffects.RESOLVE.get());
            int manhattanRange = restorationDistManhattan(resolveEffect);
            List<ItemEntity> itemsAround = world.getEntitiesOfClass(ItemEntity.class, 
                    cameraEntity.getBoundingBox().inflate(manhattanRange * 2),
                    entity -> entity.isAlive());
            Set<BlockPos> blocksPlaced = new HashSet<>();
            Set<BlockPos> blocksToForget = new HashSet<>();
            Vector3i eyePos = eyePos(cameraEntity);
            Vector3d lookVec = cameraEntity.getLookAngle();
            Vector3d eyePosD = cameraEntity.getEyePosition(1.0F);
            float staminaPerBlock = getStaminaCostPerBlock(userPower);
            int blocksToRestore = resolveEffect ? 64 : 
                Math.min(blocksPerTick(standEntity), (int) (staminaPerBlock * userPower.getStamina()));
            boolean onlyAimedAt = user.isShiftKeyDown();
            
            Stream<PrevBlockInfo> blocks = getBlocksInRange(world, user, eyePos, manhattanRange, 
                    block -> blockPosSelectedForRestoration(block, cameraEntity, lookVec, eyePosD, eyePos, resolveEffect, onlyAimedAt));
            
            blocks
            
            .filter(block -> {
                if (blockCanBePlaced(world, block.pos, block.state)) {
                    return true;
                }
                blocksToForget.add(block.pos);
                return false;
            })
            
            .sorted((bl1, bl2) -> {
                return bl1.pos.distManhattan(eyePos) - bl2.pos.distManhattan(eyePos);
            })
            
            .limit(blocksToRestore)
            
            .forEach(block -> {
                if (tryPlaceBlock(world, block.pos, block.state, blocksPlaced, 
                        creative, block.drops, block.getDroppedXp(), playerUser, userInventory, itemsAround, 
                        resolveEffect && !onlyAimedAt)) {
                    blocksToForget.add(block.pos);
                }
            });
            
            userPower.consumeStamina(staminaPerBlock * blocksPlaced.size());
            
            if (!blocksPlaced.isEmpty()) {
                PacketManager.sendToClientsTracking(new CDBlocksRestoredPacket(blocksPlaced), standEntity);
            }
            forgetBrokenBlocks(world, blocksToForget);
        }
    }
    
    private int blocksPerTick(StandEntity standEntity) {
        return MathUtil.fractionRandomInc(CrazyDiamondHeal.healingSpeed(standEntity) * 3);
    }
    

    private static final Random RANDOM = new Random();
    private static boolean tryPlaceBlock(World world, BlockPos blockPos, BlockState blockState, Set<BlockPos> placedBlocks, boolean isCreative, 
            List<ItemStack> restorationCost, int xpCost, @Nullable PlayerEntity playerWithXp, @Nullable IInventory userInventory, List<ItemEntity> itemEntities, 
            boolean randomizePos) {
        if (xpCost > 0 && (playerWithXp == null || playerWithXp.totalExperience < xpCost)) {
            return false;
        }
        if (randomizePos) {
            BlockPos randomPos = blockPos = blockPos.offset(
                    RANDOM.nextBoolean() ? RANDOM.nextInt(3) - 1 : 0, 
                    RANDOM.nextInt(2) + 1,
                    RANDOM.nextBoolean() ? RANDOM.nextInt(3) - 1 : 0);
            if (blockCanBePlaced(world, blockPos, blockState)) {
                IChunk chunk = world.getChunk(randomPos);
                if (!(chunk instanceof Chunk && ((Chunk) chunk).getCapability(ChunkCapProvider.CAPABILITY).map(cap -> cap.wasBlockBroken(randomPos)).orElse(false))) {
                    blockPos = randomPos;
                }
            }
        }
        if (blockCanBePlaced(world, blockPos, blockState) && blockState.canSurvive(world, blockPos)
                && (isCreative || consumeNeededItems(restorationCost, userInventory, itemEntities))) {
            if (!isCreative && playerWithXp != null && xpCost > 0) {
                playerWithXp.giveExperiencePoints(-xpCost);
            }
            blockState = Block.updateFromNeighbourShapes(blockState, world, blockPos);
            world.setBlockAndUpdate(blockPos, blockState);
            placedBlocks.add(blockPos);
            return true;
        }
        else {
            return false;
        }
    }
    
    public static boolean blockCanBePlaced(World world, BlockPos pos, BlockState placedBlockState) {
        return world.getBlockState(pos).getMaterial().isReplaceable();
    }
    
    private static boolean consumeNeededItems(List<ItemStack> restorationCost, @Nullable IInventory userInventory, List<ItemEntity> itemEntities) {
        if (restorationCost.size() == 1 && restorationCost.get(0).getCount() == 1) {
            return consumeSingleItem(restorationCost.get(0), userInventory, itemEntities);
        }

        List<ItemStack> costCopied = restorationCost.stream().map(ItemStack::copy).collect(Collectors.toList());
        Map<ItemStack, Pair<List<ItemStack>, MutableInt>> itemsSorted = Util.make(new HashMap<>(), map -> {
            costCopied.forEach(item -> map.put(item, Pair.of(new ArrayList<>(), new MutableInt())));
        });
        
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack lyingStack = itemEntity.getItem();
            sortItem(itemsSorted, costCopied, lyingStack);
        }
        
        if (userInventory != null) {
            int size = userInventory.getContainerSize();
            for (int i = 0; i < size; i++) {
                ItemStack inventoryItem = userInventory.getItem(i);
                sortItem(itemsSorted, costCopied, inventoryItem);
            }
        }
        
        
        if (itemsSorted.entrySet().stream().allMatch(entry -> {
            ItemStack neededItem = entry.getKey();
            Pair<List<ItemStack>, MutableInt> existingItems = entry.getValue();
            return existingItems.getRight().getValue() >= neededItem.getCount();
        })) {
            itemsSorted.entrySet().stream().forEach(entry -> {
                ItemStack neededItem = entry.getKey();
                Pair<List<ItemStack>, MutableInt> existingItems = entry.getValue();
                existingItems.getLeft().stream().anyMatch(consumedItem -> {
                    int count = Math.min(neededItem.getCount(), consumedItem.getCount());
                    consumedItem.shrink(count);
                    neededItem.shrink(count);
                    return neededItem.isEmpty();
                });
            });
            return true;
        }
        return false;
    }
    
    private static boolean consumeSingleItem(ItemStack neededSingleItem, @Nullable IInventory userInventory, List<ItemEntity> itemEntities) {
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack lyingStack = itemEntity.getItem();
            if (stacksMatch(neededSingleItem, lyingStack)) {
                lyingStack.shrink(1);
                return true;
            }
        }
        
        if (userInventory != null) {
            int size = userInventory.getContainerSize();
            for (int i = 0; i < size; i++) {
                ItemStack inventoryItem = userInventory.getItem(i);
                if (inventoryItem != null && stacksMatch(neededSingleItem, inventoryItem)) {
                    inventoryItem.shrink(1);
                    return true;
                }
            }
        }

        return false;
    }
    
    private static void sortItem(Map<ItemStack, Pair<List<ItemStack>, MutableInt>> sortMap, List<ItemStack> cost, ItemStack existingItem) {
        cost.stream().filter(costItem -> stacksMatch(costItem, existingItem)).findFirst().ifPresent(neededItem -> {
            if (!sortMap.containsKey(neededItem)) {
                // i made sure to fill the map, so it should instead fail-fast if this actually happens somehow
                return;
            }
            Pair<List<ItemStack>, MutableInt> entry = sortMap.get(neededItem);
            entry.getLeft().add(existingItem);
            entry.getRight().add(existingItem.getCount());
        });
    }
    
    private static boolean stacksMatch(ItemStack neededItem, ItemStack itemInQuestion) {
        return (!itemInQuestion.isEmpty() && itemInQuestion.getItem() == neededItem.getItem() && ItemStack.tagMatches(itemInQuestion, neededItem));
    }
    
    

    public static void addParticlesAroundBlock(World world, BlockPos blockPos, Random random) {
        if (world.isClientSide() && ClientUtil.canSeeStands()) {
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
    
    
    
    public static void rememberBrokenBlock(World world, BlockPos pos, BlockState state, Optional<TileEntity> tileEntity, List<ItemStack> drops) {
        Block block = state.getBlock();
        if (block instanceof FireBlock) return;
        
        IChunk chunk = world.getChunk(pos);
        if (chunk instanceof Chunk) {
            ((Chunk) chunk).getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.saveBrokenBlock(pos, state, tileEntity, drops);
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
    
    public static boolean blockPosSelectedForRestoration(PrevBlockInfo block, Entity cameraEntity, 
            Vector3d entityLookVec, Vector3d entityEyePos, Vector3i restorationCenter, boolean resolve, boolean aimedOnly) {
        int rangeManhattan = restorationDistManhattan(resolve);
        if (block.pos.distManhattan(restorationCenter) > rangeManhattan) {
            return false;
        }
        if (aimedOnly) {
            Vector3d pos2 = entityEyePos.add(entityLookVec.scale(rangeManhattan * 2));
            return new AxisAlignedBB(block.pos).clip(entityEyePos, pos2).isPresent();
        }
        else {
            return entityLookVec.dot(Vector3d.atCenterOf(block.pos).subtract(entityEyePos).normalize()) >= (resolve ? 0 : 0.7071);
        }
    }
    
    private static int restorationDistManhattan(boolean resolve) {
        return 12;
    }
    
    @Override
    public void phaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {
        if (world.isClientSide()) {
            if (to == Phase.PERFORM) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
                        ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, Phase.PERFORM, 1.0F, 1.0F, true);
            }
            else if (from == Phase.PERFORM) {
                standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    @Override
    public float getStaminaCostTicking(IStandPower power) {
        return 0;
    }
    
    private float getStaminaCostPerBlock(IStandPower power) {
        return super.getStaminaCostTicking(power);
    }
}
