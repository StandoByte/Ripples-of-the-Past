package com.github.standobyte.jojo.action.stand.effect;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class CDTurnIntoAngeloRockEffect extends StandEffectInstance {
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
        
        // TODO (angelo) check the target's hp
        if (false) {
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
        
        Object2IntMap<PrevBlockInfo> angeloStonePosPriority = new Object2IntArrayMap<>();
        for (PrevBlockInfo brokenBlock : brokenBlocks.values()) {
            // TODO (angelo) handle the case where there are no 2-block tall stone pillars, +the most prioritized spot can have a block at the upperBlock spot
            if (brokenBlock.state.getMaterial() != Material.STONE) {
                continue;
            }
            
            BlockPos pos = brokenBlock.pos;
            int priority = 0;
            PrevBlockInfo topBlock = brokenBlocks.get(pos.above());
            if (topBlock != null && topBlock.state.getMaterial() == Material.STONE) {
                priority += 64;
            }
            
            BlockPos checkPos = pos.below();
            if (Block.isFaceFull(getBlockAfterRestore(world, checkPos, brokenBlocks).getCollisionShape(world, checkPos), Direction.UP)) {
                priority += 32;
            }
            
            checkPos = pos.offset(0, 2, 0);
            if (getBlockAfterRestore(world, checkPos, brokenBlocks).isAir(world, checkPos)) {
                priority += 1;
            }
            
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                checkPos = pos.offset(direction.getNormal());
                if (getBlockAfterRestore(world, checkPos, brokenBlocks).isAir(world, checkPos)) {
                    priority += direction == angeloRockFace ? 8 : 2;
                }
                
                checkPos = checkPos.above();
                if (getBlockAfterRestore(world, checkPos, brokenBlocks).isAir(world, checkPos)) {
                    priority += direction == angeloRockFace ? 8 : 2;
                }
            }
            
            angeloStonePosPriority.put(brokenBlock, priority);
        }
        if (angeloStonePosPriority.isEmpty()) {
            return Action.conditionMessage("angelo_no_block_broken");
        }
        
        PrevBlockInfo blockLower;
        Optional<PrevBlockInfo> blockUpper;
        Vector3d targetPos = target.position();
        int maxPriority = angeloStonePosPriority.values().stream().max(Integer::compare).get();
        blockLower = angeloStonePosPriority.object2IntEntrySet().stream()
                .filter(entry -> entry.getIntValue() == maxPriority)
                .min(Comparator.comparingDouble(entry -> Vector3d.atCenterOf(entry.getKey().pos).distanceToSqr(targetPos)))
                .map(Object2IntMap.Entry::getKey)
                .get();
        blockUpper = Optional.ofNullable(brokenBlocks.get(blockLower.pos.above()));
        
        chunkCache.get(new ChunkPos(blockLower.pos)).ifPresent(blocksData -> {
            blocksData.removeBrokenBlock(blockLower.pos);
            blockUpper.ifPresent(block -> blocksData.removeBrokenBlock(block.pos));
        });
        brokenBlocks.remove(blockLower.pos);
        blockUpper.ifPresent(block -> brokenBlocks.remove(block.pos));
        
        // TODO (angelo) restore the rest of the blocks destroyed by the explosion
        // TODO (angelo) consume items and xp (btw mobs can also pick up dropped blocks)
        
        JojoModUtil.sayVoiceLine(user, ModSounds.JOSUKE_PRAY_FOR_ETERNITY.get(), null, 1, 1, 0, false);
        AngeloRockEntity angeloRock = AngeloRockEntity.turnIntoRock(world, target, Vector3d.atBottomCenterOf(blockLower.pos), angeloRockFace.toYRot(), 
                blockLower, blockUpper.orElse(null));
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
        angeloRockEntity.saveNbt(nbt, "Entity");
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        summonedRockEntity = nbt.getBoolean("SummonedEntity");
        angeloRockEntity.loadNbt(nbt, "Entity");
    }

}
