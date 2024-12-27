package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.github.standobyte.jojo.action.config.ActionConfigField;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.CDBlocksRestoredPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.LazySupplier;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;

public class CrazyDiamondRestoreTerrain extends StandEntityAction {
    @ActionConfigField public boolean useOtherPlayersInventories;

    public CrazyDiamondRestoreTerrain(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.getContinuousEffects().getEffects().anyMatch(
                effect -> effect.effectType == ModStandEffects.TURN_INTO_ANGELO_ROCK.get())) {
            return ActionConditionResult.NEGATIVE;
        }
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
    
    // FIXME try to mitigate the fps drops when lots of blocks are restored simultaneously
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            PlayerEntity playerUser = user instanceof PlayerEntity ? (PlayerEntity) user : null;
            boolean creative = playerUser != null ? playerUser.abilities.instabuild : false;
            Entity cameraEntity = restorationCenterEntity(user, userPower);
            boolean resolveEffect = user.hasEffect(ModStatusEffects.RESOLVE.get());
            int manhattanRange = restorationDistManhattan(resolveEffect);
            Vector3i eyePos = eyePos(cameraEntity);
            Vector3d lookVec = cameraEntity.getLookAngle();
            Vector3d eyePosD = cameraEntity.getEyePosition(1.0F);
            float staminaPerBlock = getStaminaCostPerBlock(userPower);
            int blocksToRestore = resolveEffect ? 64 : 
                Math.min(blocksPerTick(standEntity), (int) (staminaPerBlock * userPower.getStamina()));
            boolean onlyAimedAt = user.isShiftKeyDown();
            
            Stream<PrevBlockInfo> blocks = getBlocksInRange(world, user, eyePos, manhattanRange, 
                    block -> blockPosSelectedForRestoration(block, cameraEntity, lookVec, eyePosD, eyePos, resolveEffect, onlyAimedAt));
            
            AxisAlignedBB area = cameraEntity.getBoundingBox().inflate(manhattanRange * 2);
            Vector3d center = area.getCenter();
            List<ItemStack> itemsSource = sourceItemStacks(area, center, user, world, 
                    SourceType.MOB_HELD.fromAllNearby(), 
                    SourceType.ITEM_ENTITY.fromAllNearby(), 
                    playerUser != null ? SourceType.PLAYER_INVENTORY.from(playerUser) : null, 
                    useOtherPlayersInventories ? SourceType.PLAYER_INVENTORY.fromAllNearby().sort() : null);
            
            Set<BlockPos> blocksPlaced = restoreBlocks(world, standEntity, blocks, 
                    Comparator.comparingInt((PrevBlockInfo block) -> block.pos.distManhattan(eyePos)), 
                    blocksToRestore, 
                    creative, resolveEffect && !onlyAimedAt, true, 
                    playerUser, itemsSource).blocksPlaced;
            
            userPower.consumeStamina(staminaPerBlock * blocksPlaced.size());
        }
    }
    
    
    public static enum SourceType {
        MOB_HELD {
            @Override
            protected void addItems(List<ItemStack> items, Stream<Entity> entities) {
                entities.map(entity -> ((LivingEntity) entity)).forEach(mob -> {
                    for (Hand hand : Hand.values()) {
                        ItemStack item = mob.getItemInHand(hand);
                        if (!item.isEmpty()) {
                            items.add(item);
                        }
                    }
                });
            }
        },
        ITEM_ENTITY {
            @Override
            protected void addItems(List<ItemStack> items, Stream<Entity> entities) {
                entities.map(entity -> ((ItemEntity) entity).getItem())
                .filter(item -> !item.isEmpty())
                .forEach(items::add);
            }
        },
        PLAYER_INVENTORY {
            @Override
            protected void addItems(List<ItemStack> items, Stream<Entity> entities) {
                entities.map(entity -> ((PlayerEntity) entity).inventory)
                .forEach(inventory -> {
                    int size = inventory.getContainerSize();
                    for (int i = 0; i < size; i++) {
                        ItemStack inventoryItem = inventory.getItem(i);
                        if (inventoryItem != null && !inventoryItem.isEmpty()) {
                            items.add(inventoryItem);
                        }
                    }
                });
            }
        },
        OTHER {
            @Override
            protected void addItems(List<ItemStack> items, Stream<Entity> entities) {}
        };
        
        public ItemsSource fromAllNearby() {
            return new ItemsSource(this, (Entity[]) null);
        }
        
        public ItemsSource from(Entity... entity) {
            return new ItemsSource(this, entity);
        }
        
        protected abstract void addItems(List<ItemStack> items, Stream<Entity> from);
    }
    
    public static class ItemsSource {
        protected final SourceType type;
        protected boolean sort = false;
        @Nullable protected final Stream<Entity> entity;
        
        protected ItemsSource(SourceType type, Entity... entities) {
            this.type = type;
            this.entity = entities != null ? Stream.of(entities) : null;
        }
        
        public ItemsSource sort() {
            this.sort = true;
            return this;
        }
    }
    
    public static List<ItemStack> sourceItemStacks(AxisAlignedBB entitiesArea, Vector3d center, LivingEntity user, World world, 
            ItemsSource... order) {
        Map<SourceType, List<Entity>> entitiesAround = world.getEntities(user, entitiesArea,
                EntityPredicates.NO_SPECTATORS.and(e -> !e.removed))
                .stream().collect(Collectors.groupingBy(e -> {
                    if (e instanceof ItemEntity) {
                        return SourceType.ITEM_ENTITY;
                    }
                    if (e instanceof LivingEntity) {
                        if (e instanceof PlayerEntity) {
                            return SourceType.PLAYER_INVENTORY;
                        }
                        if (e instanceof MobEntity) {
                            return SourceType.MOB_HELD;
                        }
                    }
                    return SourceType.OTHER;
                }));
        List<ItemStack> itemsSource = new ArrayList<>();
        
        for (ItemsSource itemsHandler : order) {
            if (itemsHandler == null) continue;
            
            Stream<Entity> entities = itemsHandler.entity;
            if (entities == null && entitiesAround.containsKey(itemsHandler.type)) {
                entities = entitiesAround.get(itemsHandler.type).stream();
            }
            if (entities == null) continue;
            
            entities = entities.filter(Objects::nonNull);
            if (itemsHandler.sort) {
                entities = entities.sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)));
            }
            itemsHandler.type.addItems(itemsSource, entities);
        }
        
        return itemsSource;
    }
    
    
    private int blocksPerTick(StandEntity standEntity) {
        return MathUtil.fractionRandomInc(CrazyDiamondHeal.healingSpeed(standEntity) * 3);
    }
    
    private static final Random RANDOM = new Random();
    public static RestoreResult restoreBlocks(World world, Entity trackedEntity, Stream<PrevBlockInfo> blocks, 
            Comparator<PrevBlockInfo> sort, long limit, 
            boolean isCreative, boolean randomizePos, boolean forgetFailed, 
            @Nullable PlayerEntity playerWithXp, List<ItemStack> itemsSource) {
        RestoreResult result = new RestoreResult();
        if (limit == 0) return result;
        
        blocks = blocks
        .filter(block -> {
            if (restorationExclude(block, world)) {
                return false;
            }
            if (blockCanBePlaced(world, block.pos, block.state)) {
                return true;
            }
            if (forgetFailed) {
                result.blocksToForget.add(block.pos);
            }
            return false;
        });
        if (sort != null) {
            sort = Comparator.comparingInt((PrevBlockInfo block) -> restorationPriority(block, world))
                    .thenComparing(sort);
            blocks = blocks.sorted(sort);
        }
        if (limit >= 0) {
            blocks = blocks.limit(limit);
        }
        
        blocks.forEach(block -> {
            if (block.onRestore()) {
                result.blocksTried.add(block.pos);
                if (tryPlaceBlock(world, block.pos, block.state, isCreative, randomizePos, 
                    block.drops, block.getDroppedXp(), playerWithXp, itemsSource)) {
                    result.blocksPlaced.add(block.pos);
                    result.blocksToForget.add(block.pos);
                }
            }
        });
        
        if (!result.blocksPlaced.isEmpty()) {
            PacketManager.sendToClientsTrackingAndSelf(new CDBlocksRestoredPacket(result.blocksPlaced), trackedEntity);
        }
        forgetBrokenBlocks(world, result.blocksToForget);
        
        return result;
    }
    
    public static class RestoreResult {
        public final Set<BlockPos> blocksTried = new HashSet<>();
        public final Set<BlockPos> blocksPlaced = new HashSet<>();
        public final Set<BlockPos> blocksToForget = new HashSet<>();
    }
    
    // this whole junk fixes janky restoration of sand blocks, e.g. explosions in a desert
    private static boolean restorationExclude(PrevBlockInfo block, World world) {
        if (block.state.getBlock() instanceof FallingBlock) {
            BlockPos blockBelow = block.pos.below();
            if (world.isEmptyBlock(blockBelow)) {
                IChunk chunk = world.getChunk(block.pos);
                if (chunk instanceof Chunk) {
                    boolean blockBelowCanBeRestored = ((Chunk) chunk).getCapability(ChunkCapProvider.CAPABILITY).map(cap -> {
                        return cap.getBrokenBlocks().anyMatch(brokenBlock -> blockBelow.equals(brokenBlock.pos));
                    }).orElse(false);
                    
                    if (blockBelowCanBeRestored) {
                        return true;
                    }
                }
            }
        }
        
        return !block.state.canSurvive(world, block.pos);
    }
    
    private static int restorationPriority(PrevBlockInfo block, World world) {
        if (block.state.getBlock() instanceof FallingBlock && !world.isEmptyBlock(block.pos.below())) {
            return 1;
        }
        return 2;
    }
    
    private static boolean tryPlaceBlock(World world, BlockPos blockPos, BlockState blockState, boolean isCreative, boolean randomizePos, 
            List<ItemStack> restorationCost, int xpCost, @Nullable PlayerEntity consumeXpFrom, List<ItemStack> itemsSource) {
        if (xpCost > 0 && (consumeXpFrom == null || consumeXpFrom.totalExperience < xpCost)) {
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
                && (consumeNeededItems(restorationCost, itemsSource, null) || isCreative)) {
            if (!isCreative && consumeXpFrom != null && xpCost > 0) {
                consumeXpFrom.giveExperiencePoints(-xpCost);
            }
            blockState = Block.updateFromNeighbourShapes(blockState, world, blockPos);
            world.setBlockAndUpdate(blockPos, blockState);
            return true;
        }
        else {
            return false;
        }
    }
    
    public static boolean blockCanBePlaced(World world, BlockPos pos, BlockState placedBlockState) {
        return world.getBlockState(pos).getMaterial().isReplaceable();
    }
    
    public static boolean consumeNeededItems(List<ItemStack> restorationCost, List<ItemStack> itemsSource, 
            @Nullable List<ItemStack> collectConsumedItems) {
        if (restorationCost.isEmpty()) {
            return true;
        }
        if (restorationCost.size() == 1 && restorationCost.get(0).getCount() == 1) {
            return consumeSingleItem(restorationCost.get(0), itemsSource, collectConsumedItems);
        }

        List<ItemStack> costCopied = restorationCost.stream().map(ItemStack::copy).collect(Collectors.toList());
        Map<ItemStack, Pair<List<ItemStack>, MutableInt>> itemsFound = Util.make(new HashMap<>(), map -> {
            costCopied.forEach(item -> map.put(item, Pair.of(new ArrayList<>(), new MutableInt())));
        });
        
        for (ItemStack item : itemsSource) {
            sortItem(itemsFound, costCopied, item);
        }
        
        
        if (itemsFound.entrySet().stream().allMatch(entry -> {
            ItemStack neededItem = entry.getKey();
            Pair<List<ItemStack>, MutableInt> existingItems = entry.getValue();
            return existingItems.getRight().getValue() >= neededItem.getCount();
        })) {
            itemsFound.entrySet().stream().forEach(entry -> {
                ItemStack neededItem = entry.getKey();
                Pair<List<ItemStack>, MutableInt> existingItems = entry.getValue();
                existingItems.getLeft().stream().anyMatch(consumedItem -> {
                    int count = Math.min(neededItem.getCount(), consumedItem.getCount());
                    if (collectConsumedItems != null) {
                        ItemStack remembered = consumedItem.copy();
                        remembered.setCount(count);
                    }
                    consumedItem.shrink(count);
                    neededItem.shrink(count);
                    return neededItem.isEmpty();
                });
            });
            return true;
        }
        return false;
    }
    
    private static boolean consumeSingleItem(ItemStack neededSingleItem, List<ItemStack> itemsSource, 
            @Nullable List<ItemStack> collectConsumedItems) {
        for (ItemStack item : itemsSource) {
            if (stacksMatch(neededSingleItem, item)) {
                if (collectConsumedItems != null) {
                    ItemStack remember = item.copy();
                    remember.setCount(1);
                    collectConsumedItems.add(remember);
                }
                item.shrink(1);
                return true;
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
    
    public static void forgetBrokenBlocks(World world, Collection<BlockPos> posCollection) {
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
    
    public float getStaminaCostPerBlock(IStandPower power) {
        return super.getStaminaCostTicking(power);
    }
    
    private final LazySupplier<ResourceLocation> resolveTex = new LazySupplier<>(() -> makeIconVariant(this, "_improper"));
    @Override
    public ResourceLocation getIconTexturePath(@Nullable IStandPower power) {
        if (power != null) {
            LivingEntity user = power.getUser();
            if (user != null && user.hasEffect(ModStatusEffects.RESOLVE.get())) {
                return resolveTex.get();
            }
        }
        
        return super.getIconTexturePath(power);
    }
}
