package com.github.standobyte.jojo.action.stand.effect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain.RestoreResult;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain.SourceType;
import com.github.standobyte.jojo.capability.chunk.ChunkCap;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.CDBlocksRestoredPacket;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;

public class CDTurnIntoAngeloRockEffect extends StandEffectInstance {
    public boolean keepMobsInside;
    private boolean triedSummonRockEntity = false;
    private EntityOwnerResolver.Generic<AngeloRockEntity> angeloRockEntity = new EntityOwnerResolver.Generic<>(AngeloRockEntity.class);
    
    private Map<BlockPos, PrevBlockInfo> brokenBlocks;
    private Set<BlockPos> failedToRestore = new HashSet<>();
    private boolean restoreAllBlocksFailed;
    @Nullable private BlockPos lastTargetPos;

    public CDTurnIntoAngeloRockEffect() {
        this(ModStandEffects.TURN_INTO_ANGELO_ROCK.get());
    }

    public CDTurnIntoAngeloRockEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    @Override
    protected void start() {}
    
    protected void tickTarget(LivingEntity target) {
        if (!target.level.isClientSide()) {
            if (!triedSummonRockEntity) {
                KnockbackCollisionImpact kbCollision = KnockbackCollisionImpact.getHandler(target).orElse(null);
                if (kbCollision == null) {
                    remove();
                    return;
                }
                if (kbCollision.isActive()) {
                    return;
                }
                else {
                    ActionConditionResult tryStartAngeloRock = tryStartAngeloRock(target, kbCollision);
                    if (!tryStartAngeloRock.isPositive()) {
                        ActionConditionResult.sendActionFailedMessage(ModStandsInit.CRAZY_DIAMOND_ANGELO_ROCK.get(), tryStartAngeloRock, user);
                    }
                    triedSummonRockEntity = true;
                }
            }
        }
    }
    
    @Override
    protected void tick() {
        if (!world.isClientSide()) {
            LivingEntity target = getTargetLiving();
            if (target != null) {
                tickTarget(target);
                if (toBeRemoved()) {
                    return;
                }
            }
            
            boolean hasBlocksToRestore = hasBlocksToRestore();
            if (triedSummonRockEntity || getTarget() == null) {
                AngeloRockEntity angeloRock = angeloRockEntity.getEntityCast(world);
                if ((angeloRock == null || angeloRock.isFullyFormed()) && !hasBlocksToRestore) {
                    remove();
                    return;
                }
            }
            if (hasBlocksToRestore) {
                Entity entity = angeloRockEntity.getEntity(world);
                BlockPos centerPos;
                if (entity == null) {
                    entity = getTarget();
                }
                
                if (entity != null) {
                    centerPos = entity.blockPosition();
                }
                else {
                    centerPos = lastTargetPos;
                }
                
                if (centerPos == null)  {
                    entity = getStandUser();
                    if (entity != null) {
                        centerPos = entity.blockPosition();
                    }
                }
                
                if (centerPos != null) {
                    restoreBrokenBlocks(centerPos);
                }
            }
        }
    }
    
    @Override
    protected void stop() {
        AngeloRockEntity angeloRock = angeloRockEntity.getEntityCast(world);
        if (angeloRock != null && !angeloRock.isFullyFormed()) {
            angeloRock.breakRock();
        }
    }

    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        nbt.putBoolean("TriedSummonRock", triedSummonRockEntity);
        nbt.putBoolean("SaveMob", keepMobsInside);
        angeloRockEntity.saveNbt(nbt, "Entity");
        
        if (brokenBlocks != null && !brokenBlocks.isEmpty()) {
            ListNBT blocksBrokenNbt = new ListNBT();
            for (PrevBlockInfo block : brokenBlocks.values()) {
                blocksBrokenNbt.add(block.toNBT());
            }
            nbt.put("FixBlocks", blocksBrokenNbt);
        }
        
        if (lastTargetPos != null) {
            nbt.put("LastTargetPos", NBTUtil.writeBlockPos(lastTargetPos));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        triedSummonRockEntity = nbt.getBoolean("TriedSummonRock");
        keepMobsInside = nbt.getBoolean("SaveMob");
        angeloRockEntity.loadNbt(nbt, "Entity");
        
        brokenBlocks = null;
        ListNBT blocksBrokenNbt = nbt.getList("FixBlocks", Constants.NBT.TAG_COMPOUND);
        if (!blocksBrokenNbt.isEmpty()) {
            brokenBlocks = new HashMap<>();
            blocksBrokenNbt.forEach(blockNBT -> {
                PrevBlockInfo block = PrevBlockInfo.fromNBT((CompoundNBT) blockNBT);
                if (block != null) {
                    brokenBlocks.put(block.pos, block);
                }
            });
        }
        
        lastTargetPos = MCUtil.nbtGetCompoundOptional(nbt, "LastTargetPos").map(blockPosNbt -> NBTUtil.readBlockPos(blockPosNbt)).orElse(null);
    }
    
    public boolean preventTargetDeath() {
        if (!triedSummonRockEntity) {
            return true;
        }
        Entity angeloRock = angeloRockEntity.getEntity(world);
        return angeloRock != null && angeloRock.isAlive();
    }
    
    
    private static boolean canUseBlock(BlockState blockState) {
        return blockState.getMaterial() == Material.STONE;
    }
    
    @SuppressWarnings("deprecation")
    private ActionConditionResult tryStartAngeloRock(LivingEntity target, KnockbackCollisionImpact kbCollision) {
        List<BlockPos> brokenBlocksPos = kbCollision.blocksDestroyedByLastExplosion;
        if (brokenBlocksPos == null || brokenBlocksPos.isEmpty()) {
            return Action.conditionMessage("angelo_no_block_broken");
        }
        
        Map<ChunkPos, Optional<ChunkCap>> chunkCache = new HashMap<>();
        brokenBlocks = brokenBlocksPos.stream()
                .map(blockPos -> {
                    ChunkPos chunkPos = new ChunkPos(blockPos);
                    Optional<ChunkCap> chunkData = chunkCache.computeIfAbsent(chunkPos, pos -> {
                        Chunk chunk = target.level.getChunk(pos.x, pos.z);
                        return Optional.ofNullable(chunk).flatMap(c -> c.getCapability(ChunkCapProvider.CAPABILITY).resolve());
                    });
                    PrevBlockInfo prevBlock = chunkData.map(chunk -> chunk.getBrokenBlockAt(blockPos)).orElse(null);
                    return prevBlock;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(block -> block.pos, Function.identity()));
        
        // rebalance if necessary
        float hpLimit = Math.max(10, target.getHealth() * 0.05f);
        if (target.getHealth() > hpLimit) {
            return Action.conditionMessage("target_too_many_health");
        }
        
        Map<BlockPos, BlockWithDist> brokenStoneBlocks = brokenBlocks.entrySet().stream()
                .filter(entry -> canUseBlock(entry.getValue().state))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new BlockWithDist(entry.getValue())));
        if (brokenStoneBlocks.size() < 2) {
            return Action.conditionMessage("angelo_no_stone_broken");
        }
        
        Vector3d targetPos = target.position();
        Optional<FindBlockEntry> closestAngeloRockBlocks = brokenStoneBlocks.values().stream()
                .map(entry -> {
                    PrevBlockInfo block = entry.block;

                    BlockPos posAbove = block.pos.above();
                    BlockPos posBelow = block.pos.below();

                    BlockWithDist brokenBlockAbove = brokenStoneBlocks.get(posAbove);
                    if (brokenBlockAbove != null && entry.getDistLower(targetPos) <= 3) {
                        return new FindBlockEntry(entry, brokenBlockAbove, 1);
                    }
                    BlockWithDist brokenBlockBelow = brokenStoneBlocks.get(posBelow);
                    if (brokenBlockBelow != null && brokenBlockBelow.getDistLower(targetPos) <= 3) {
                        return new FindBlockEntry(brokenBlockBelow, entry, 1);
                    }

                    if (!brokenBlocks.containsKey(posAbove)) {
                        BlockState curBlockAbove = world.getBlockState(posAbove);
                        if (curBlockAbove.isAir(world, posAbove)) {
                            return new FindBlockEntry(entry, null, 2);
                        }
                        else if (canUseBlock(curBlockAbove)) {
                            return new FindBlockEntry(entry, null, 2).breakUpperBlock(posAbove);
                        }
                    }
                    if (!brokenBlocks.containsKey(posBelow)) {
                        BlockState curBlockBelow = world.getBlockState(posBelow);
                        if (curBlockBelow.isAir(world, posBelow)) {
                            return new FindBlockEntry(null, entry, 2);
                        }
                        else if (canUseBlock(curBlockBelow)) {
                            return new FindBlockEntry(null, entry, 2).breakLowerBlock(posBelow);
                        }
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt((FindBlockEntry entry) -> entry.priority).thenComparing(
                        Comparator.comparingDouble((FindBlockEntry entry) -> entry.getDist(targetPos))))
                .findAny();
        if (!closestAngeloRockBlocks.isPresent()) {
            return Action.conditionMessage("angelo_no_stone_broken");
        }
        
        FindBlockEntry angeloRockBlocks = closestAngeloRockBlocks.get();
        ChunkCap blocksData = chunkCache.get(angeloRockBlocks.getChunkPos()).orElse(null);
        angeloRockBlocks.resolveAngeloBlocks(world, brokenStoneBlocks, blocksData, targetPos, MCUtil.dropBrokenBlock(user));
        
        if (angeloRockBlocks.lower == null || angeloRockBlocks.lower.block == null || angeloRockBlocks.upper == null || angeloRockBlocks.upper.block == null) {
            return Action.conditionMessage("angelo_no_stone_broken");
        }
        @Nonnull PrevBlockInfo blockLower = angeloRockBlocks.lower.block;
        @Nonnull PrevBlockInfo blockUpper = angeloRockBlocks.upper.block;
        if (blocksData != null) {
            blocksData.removeBrokenBlock(blockLower.pos);
            blocksData.removeBrokenBlock(blockUpper.pos);
        }
        brokenBlocks.remove(blockLower.pos);
        brokenBlocks.remove(blockUpper.pos);
        
        if (!blockUpper.pos.equals(blockLower.pos.above())) {
            blockUpper = new PrevBlockInfo(blockLower.pos.above(), blockUpper.state, blockUpper.drops, blockUpper.keep);
        }
        if (!blockLower.pos.equals(blockUpper.pos.below())) {
            blockLower = new PrevBlockInfo(blockUpper.pos.below(), blockLower.state, blockLower.drops, blockLower.keep);
        }
        
        // FIXME test if the block drops match
        JojoModUtil.sayVoiceLine(user, ModSounds.JOSUKE_PRAY_FOR_ETERNITY.get(), null, 1, 1, 0, false);
        Direction angeloRockFace = Direction.fromYRot(target.yRot);
        AngeloRockEntity angeloRock = AngeloRockEntity.turnIntoRock(world, target, 
                keepMobsInside && target instanceof MobEntity ? (MobEntity) target : null, 
                Vector3d.atBottomCenterOf(blockLower.pos), angeloRockFace.toYRot(), 
                blockLower, blockUpper);
        this.angeloRockEntity.setOwner(angeloRock);
        
        List<ItemStack> itemsSource = itemsSource(angeloRock.blockPosition());
        List<ItemStack> blockDrops = new ArrayList<>();
        CrazyDiamondRestoreTerrain.consumeNeededItems(blockUpper.drops, itemsSource, blockDrops);
        CrazyDiamondRestoreTerrain.consumeNeededItems(blockLower.drops, itemsSource, blockDrops);
        angeloRock.setBlockDrops(blockDrops);
        
        return ActionConditionResult.POSITIVE;
    }
    
    private static class BlockWithDist {
        public final PrevBlockInfo block;
        private double distLower = -1;
        private double distUpper = -1;
        
        private BlockWithDist(PrevBlockInfo block) {
            this.block = block;
        }
        
        public double getDistLower(Vector3d targetPos) {
            if (distLower == -1) {
                distLower = targetPos.distanceToSqr(block.pos.getX() + 0.5, block.pos.getY(), block.pos.getZ() + 0.5);
            }
            return distLower;
        }
        
        public double getDistUpper(Vector3d targetPos) {
            if (distUpper == -1) {
                distUpper = targetPos.distanceToSqr(block.pos.getX() + 0.5, block.pos.getY() + 1, block.pos.getZ() + 0.5);
            }
            return distUpper;
        }
    }
    
    private static class FindBlockEntry {
        public BlockWithDist lower;
        public BlockWithDist upper;
        public BlockPos breakLowerStone;
        public BlockPos breakUpperStone;
        public final int priority;
        
        private FindBlockEntry(BlockWithDist lower, BlockWithDist upper, int priority) {
            this.lower = lower;
            this.upper = upper;
            this.priority = priority;
        }
        
        public FindBlockEntry breakLowerBlock(BlockPos blockPos) {
            breakLowerStone = blockPos;
            return this;
        }
        
        public FindBlockEntry breakUpperBlock(BlockPos blockPos) {
            breakUpperStone = blockPos;
            return this;
        }
        
        public double getDist(Vector3d targetPos) {
            if (lower != null) {
                return lower.getDistLower(targetPos);
            }
            else if (upper != null) {
                return upper.getDistUpper(targetPos);
            }
            throw new IllegalStateException();
        }
        
        public ChunkPos getChunkPos() {
            if (lower != null) {
                return new ChunkPos(lower.block.pos);
            }
            else if (upper != null) {
                return new ChunkPos(upper.block.pos);
            }
            throw new IllegalStateException();
        }
        
        public void resolveAngeloBlocks(World world, Map<BlockPos, BlockWithDist> brokenStoneBlocks, 
                ChunkCap blocksData, Vector3d targetPos, boolean dropBlock) {
            if (lower == null && upper == null) {
                throw new IllegalStateException();
            }
            if (lower == null) {
                if (breakLowerStone != null) {
                    MCUtil.destroyBlock(world, breakLowerStone, dropBlock, null);
                    lower = new BlockWithDist(blocksData.getBrokenBlockAt(breakLowerStone));
                }
                else {
                    brokenStoneBlocks.remove(upper.block.pos);
                    lower = brokenStoneBlocks.values().stream().min(Comparator.comparingDouble(entry -> entry.getDistLower(targetPos))).get();
                }
            }
            else if (upper == null) {
                if (breakUpperStone != null) {
                    MCUtil.destroyBlock(world, breakUpperStone, dropBlock, null);
                    upper = new BlockWithDist(blocksData.getBrokenBlockAt(breakUpperStone));
                }
                else {
                    brokenStoneBlocks.remove(lower.block.pos);
                    upper = brokenStoneBlocks.values().stream().min(Comparator.comparingDouble(entry -> entry.getDistUpper(targetPos))).get();
                }
            }
        }
    }
    
    
    @Override
    protected boolean needsTarget() {
        return !hasBlocksToRestore();
    }
    
    @Override
    protected void setTargetEntity(Entity target) {
        Entity curTarget = getTarget();
        if (curTarget != null && !curTarget.isAlive()) {
            lastTargetPos = curTarget.blockPosition();
        }
        super.setTargetEntity(target);
    }
    
    private boolean hasBlocksToRestore() {
        return brokenBlocks != null && !brokenBlocks.isEmpty();
    }
    
    protected void restoreBrokenBlocks(BlockPos center) {
        LivingEntity user = getStandUser();
        int limit = 2;
        RestoreResult result = CrazyDiamondRestoreTerrain.restoreBlocks(world, user, 
                brokenBlocks.values().stream().filter(block -> !failedToRestore.contains(block.pos)), 
                Comparator.comparingInt((PrevBlockInfo block) -> block.pos.distManhattan(center)), 
                limit, 
                !MCUtil.dropBrokenBlock(user), false, true, 
                null, itemsSource(center));
        
        boolean restoredThisTick = !result.blocksToForget.isEmpty();
        if (restoredThisTick && !result.blocksTried.isEmpty()) {
            result.blocksToForget.forEach(brokenBlocks::remove);
            restoreAllBlocksFailed = false;
        }
        else {
            restoreAllBlocksFailed |= failedToRestore.isEmpty();
            failedToRestore.addAll(result.blocksTried);
            if (result.blocksTried.isEmpty() || failedToRestore.size() >= brokenBlocks.size()) {
                if (restoreAllBlocksFailed) {
                    // one of the 2 conditions to remove the effect
                    brokenBlocks.clear();
                }
                else {
                    failedToRestore.clear();
                }
            }
        }
        
        if (!result.blocksPlaced.isEmpty()) {
            PacketManager.sendToClientsTrackingAndSelf(new CDBlocksRestoredPacket(result.blocksPlaced), user);
        }
    }
    
    protected List<ItemStack> itemsSource(BlockPos center) {
        AxisAlignedBB area = new AxisAlignedBB(center, center).inflate(8);
        LivingEntity target = getTargetLiving();
        List<ItemStack> itemsSource = CrazyDiamondRestoreTerrain.sourceItemStacks(area, Vector3d.atBottomCenterOf(center), user, world, 
                target instanceof PlayerEntity ? SourceType.PLAYER_INVENTORY.from(target) : null, 
                SourceType.MOB_HELD.fromAllNearby(), 
                SourceType.ITEM_ENTITY.fromAllNearby(), 
                user instanceof PlayerEntity ? SourceType.PLAYER_INVENTORY.from(user) : null, 
                ModStandsInit.CRAZY_DIAMOND_RESTORE_TERRAIN.get().useOtherPlayersInventories ? SourceType.PLAYER_INVENTORY.fromAllNearby().sort() : null);
        return itemsSource;
    }
}
