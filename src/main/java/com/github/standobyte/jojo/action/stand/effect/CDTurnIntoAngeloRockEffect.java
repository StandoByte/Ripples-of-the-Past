package com.github.standobyte.jojo.action.stand.effect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockBullet;
import com.github.standobyte.jojo.capability.chunk.ChunkCap;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

// TODO (angelo) if the target entity dies while the stand has this modifier, keep them at minumum hp (cancel the death event)
public class CDTurnIntoAngeloRockEffect extends StandEffectInstance {

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
            KnockbackCollisionImpact kbCollision = KnockbackCollisionImpact.getHandler(target).orElse(null);
            if (kbCollision == null) {
                remove();
                return;
            }
            if (kbCollision.isActive()) {
                return;
            }
            else {
                ActionConditionResult createRock = ActionConditionResult.POSITIVE;
                List<BlockPos> brokenBlocks = kbCollision.blocksDestroyedByLastExplosion;
                if (brokenBlocks == null || brokenBlocks.isEmpty()) {
                    createRock = Action.conditionMessage("angelo_no_block_broken");
                }
                
                Map<BlockPos, BlockState> blockMap = null;
                List<ItemStack> itemDrops = null;
                if (createRock.isPositive()) {
                    // TODO (angelo) item drops
                    Map<ChunkPos, Optional<ChunkCap>> chunkCache = new HashMap<>();
                    blockMap = brokenBlocks.stream()
                            .map(blockPos -> {
                                ChunkPos chunkPos = new ChunkPos(blockPos);
                                Optional<ChunkCap> chunkData = chunkCache.computeIfAbsent(chunkPos, pos -> {
                                    Chunk chunk = target.level.getChunk(pos.x, pos.z);
                                    return Optional.ofNullable(chunk).flatMap(c -> c.getCapability(ChunkCapProvider.CAPABILITY).resolve());
                                });
                                PrevBlockInfo prevBlock = chunkData.map(chunk -> chunk.getBrokenBlockAt(blockPos)).orElse(null);
                                if (prevBlock == null) return null;
                                BlockState blockState = prevBlock.state;
                                // TODO (angelo) should it be limited to solid blocks or rock blocks? (angelo_no_block_broken message)
                                if (!CrazyDiamondBlockBullet.hardMaterial(blockState)) return null;
                                // TODO (angelo) consume items and xp
                                // TODO (angelo) btw mobs can also pick up dropped blocks
                                prevBlock.onRestore();
                                // TODO (angelo) adjust the alrogithm for block breaking from kb impact, then uncomment this
//                                chunkData.ifPresent(chunk -> chunk.removeBrokenBlock(blockPos));
                                return Pair.of(blockPos, prevBlock);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toMap(Pair::getKey, entry -> entry.getValue().state));
                    if (blockMap.isEmpty()) {
                        createRock = Action.conditionMessage("angelo_no_block_broken");
                    }
                }
                
                if (createRock.isPositive()) {
                    // TODO (angelo) check the target's hp
                    if (false) {
                        createRock = Action.conditionMessage("target_too_many_health");
                    }
                }
                
                if (createRock.isPositive()) {
                    JojoModUtil.sayVoiceLine(user, ModSounds.JOSUKE_PRAY_FOR_ETERNITY.get(), null, 1, 1, 0, false);
                    // TODO (angelo) find the 2 blocks to use for angelo rock creation, restore the rest of the blocks destroyed by the explosion (non-rock blocks too)
                    BlockState blockUpper = Blocks.GRANITE.defaultBlockState();
                    BlockState blockLower = Blocks.DIORITE.defaultBlockState();
                    AngeloRockEntity.turnIntoRock(target, blockUpper, blockLower, itemDrops);
                    createRock = ActionConditionResult.POSITIVE;
                }
                else if (user instanceof ServerPlayerEntity) {
                    ActionConditionResult.sendActionFailedMessage(ModStandsInit.CRAZY_DIAMOND_ANGELO_ROCK.get(), createRock, user);
                }
                remove();
            }
        }
    }

    @Override
    protected void tick() {}

    @Override
    protected void stop() {}

}
