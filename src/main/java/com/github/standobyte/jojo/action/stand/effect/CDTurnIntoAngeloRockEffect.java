package com.github.standobyte.jojo.action.stand.effect;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.capability.chunk.ChunkCap;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class CDTurnIntoAngeloRockEffect extends StandEffectInstance {
    public boolean keepMobsInside;
    private boolean summonedRockEntity = false;
    private EntityOwnerResolver.Generic<AngeloRockEntity> angeloRockEntity = new EntityOwnerResolver.Generic<>(AngeloRockEntity.class);

    public CDTurnIntoAngeloRockEffect() {
        this(ModStandEffects.TURN_INTO_ANGELO_ROCK.get());
    }

    public CDTurnIntoAngeloRockEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    @Override
    protected boolean needsTarget() {
        return true;
    }

    @Override
    protected void start() {}
    
    @Override
    protected void tickTarget(LivingEntity target) {
        if (!target.level.isClientSide()) {
            if (!summonedRockEntity) {
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
                    if (tryStartAngeloRock.isPositive()) {
                        summonedRockEntity = true;
                    }
                    else {
                        if (user instanceof ServerPlayerEntity) {
                            ActionConditionResult.sendActionFailedMessage(ModStandsInit.CRAZY_DIAMOND_ANGELO_ROCK.get(), tryStartAngeloRock, user);
                        }
                        remove();
                    }
                }
            }
            else {
                AngeloRockEntity angeloRock = angeloRockEntity.getEntityCast(world);
                if (angeloRock == null || angeloRock.isFullyFormed()) {
                    remove();
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private ActionConditionResult tryStartAngeloRock(LivingEntity target, KnockbackCollisionImpact kbCollision) {
        List<BlockPos> brokenBlocksPos = kbCollision.blocksDestroyedByLastExplosion;
        if (brokenBlocksPos == null || brokenBlocksPos.isEmpty()) {
            return Action.conditionMessage("angelo_no_block_broken");
        }
        
        // rebalance if necessary
        float hpLimit = Math.max(10, target.getHealth() * 0.05f);
        if (target.getHealth() > hpLimit) {
            return Action.conditionMessage("target_too_many_health");
        }
        
        Direction angeloRockFace = Direction.fromYRot(target.yRot);
        Map<ChunkPos, Optional<ChunkCap>> chunkCache = new HashMap<>();
        Map<BlockPos, PrevBlockInfo> brokenBlocks = brokenBlocksPos.stream()
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
        
        Vector3d targetPos = target.position();
        PrevBlockInfo blockLower;
        PrevBlockInfo blockUpper;
        
        Map<BlockPos, Pair<PrevBlockInfo, Double>> brokenStoneBlocks = brokenBlocks.entrySet().stream()
                .filter(entry -> entry.getValue().state.getMaterial() == Material.STONE)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    PrevBlockInfo block = entry.getValue();
                    return Pair.of(block, targetPos.distanceToSqr(block.pos.getX() + 0.5, block.pos.getY(), block.pos.getZ() + 0.5));
                }));
        if (brokenStoneBlocks.size() < 2) {
            return Action.conditionMessage("angelo_no_block_broken");
        }
        blockLower = brokenStoneBlocks.values().stream()
                // a block with another stone block above it and that's close enough
                .filter(block -> brokenStoneBlocks.containsKey(block.getKey().pos.above()))
                .filter(block -> block.getValue() <= 3)
                .min(Comparator.comparingDouble(Pair::getRight)).map(Pair::getKey)
                .orElseGet(() -> brokenStoneBlocks.values().stream()
                        // or just the closest block
                        .min(Comparator.comparingDouble(Pair::getRight)).map(Pair::getKey).get());
        brokenStoneBlocks.remove(blockLower.pos);
        
        blockUpper = Optional.ofNullable(brokenStoneBlocks.get(blockLower.pos.above())).map(Pair::getKey)
                .orElseGet(() -> brokenStoneBlocks.values().stream().map(Pair::getKey).min(Comparator.comparingDouble(block -> {
                    return targetPos.distanceToSqr(block.pos.getX() + 0.5, block.pos.getY() + 1, block.pos.getZ() + 0.5);
                })).get());
        
        ChunkCap blocksData = chunkCache.get(new ChunkPos(blockLower.pos)).orElse(null);
        if (blocksData != null) {
            blocksData.removeBrokenBlock(blockLower.pos);
            blocksData.removeBrokenBlock(blockUpper.pos);
        }
        brokenBlocks.remove(blockLower.pos);
        brokenBlocks.remove(blockUpper.pos);
        
        // TODO (angelo) restore the rest of the blocks destroyed by the explosion
        // TODO (angelo) consume items (btw mobs can also pick up dropped blocks)
        
        blockUpper = new PrevBlockInfo(blockLower.pos.above(), blockUpper.state, blockUpper.drops, blockUpper.keep);
        
        JojoModUtil.sayVoiceLine(user, ModSounds.JOSUKE_PRAY_FOR_ETERNITY.get(), null, 1, 1, 0, false);
        AngeloRockEntity angeloRock = AngeloRockEntity.turnIntoRock(world, target, 
                keepMobsInside && target instanceof MobEntity ? (MobEntity) target : null, 
                Vector3d.atBottomCenterOf(blockLower.pos), angeloRockFace.toYRot(), 
                blockLower, blockUpper);
        this.angeloRockEntity.setOwner(angeloRock);
        
        return ActionConditionResult.POSITIVE;
    }
    
    private static BlockState getBlockAfterRestore(World world, BlockPos blockPos, Map<BlockPos, PrevBlockInfo> brokenBlocks) {
        PrevBlockInfo willRestore = brokenBlocks.get(blockPos);
        if (willRestore != null && CrazyDiamondRestoreTerrain.blockCanBePlaced(world, blockPos, willRestore.state)) {
            return willRestore.state;
        }
        else {
            return world.getBlockState(blockPos);
        }
    }

    @Override
    protected void tick() {}

    @Override
    protected void stop() {
        AngeloRockEntity angeloRock = angeloRockEntity.getEntityCast(world);
        if (angeloRock != null && !angeloRock.isFullyFormed()) {
            angeloRock.breakRock();
        }
    }

    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        nbt.putBoolean("SummonedEntity", summonedRockEntity);
        nbt.putBoolean("SaveMob", keepMobsInside);
        angeloRockEntity.saveNbt(nbt, "Entity");
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        summonedRockEntity = nbt.getBoolean("SummonedEntity");
        keepMobsInside = nbt.getBoolean("SaveMob");
        angeloRockEntity.loadNbt(nbt, "Entity");
    }

}
