package com.github.standobyte.jojo.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.ChunkSectionPos;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.advancements.criterion.PlacedBlockTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

@Mixin(PlacedBlockTrigger.class)
public class PlacedBlockTriggerMixin {
    
    @Inject(method = "trigger", at = @At("TAIL"))
    public void jojoPlacedBlockAdvancement(ServerPlayerEntity player, BlockPos blockPos, ItemStack item, CallbackInfo ci) {
        if (player.level.dimension() == ModDimensions.MR_PRESIDENT) {
            checkRoomAdvancement(player, blockPos);
        }
    }

    private static final List<BlockPos> BLOCK_OFFSETS;
    private static final List<BlockPos> BLOCK_OFFSETS_TOP;
    static {
        BLOCK_OFFSETS = new ArrayList<>();
        BLOCK_OFFSETS_TOP = new ArrayList<>();
        for (int x = 4; x <= 11; x++) {
            for (int y = 6; y <= 9; y++) {
                BLOCK_OFFSETS.add(new BlockPos(x, y, 3));
                BLOCK_OFFSETS.add(new BlockPos(x, y, 12));
            }
        }
        for (int z = 4; z <= 11; z++) {
            for (int y = 6; y <= 9; y++) {
                BLOCK_OFFSETS.add(new BlockPos(3, y, z));
                BLOCK_OFFSETS.add(new BlockPos(12, y, z));
            }
        }
        for (int x = 4; x <= 11; x++) {
            for (int z = 4; z <= 11; z++) {
                BLOCK_OFFSETS.add(new BlockPos(x, 5, z));
                BLOCK_OFFSETS_TOP.add(new BlockPos(x, 10, z));
            }
        }
    }
    private static void checkRoomAdvancement(ServerPlayerEntity player, BlockPos blockPos) {
        boolean hasWalls = true;
        ChunkSectionPos roomPos = new ChunkSectionPos(blockPos);
        BlockPos checkBlockPos;
        BlockState blockState;
        for (BlockPos offset : BLOCK_OFFSETS) {
            checkBlockPos = roomPos.blockPosition(offset);
            blockState = player.level.getBlockState(checkBlockPos);
            if (blockState.isAir(player.level, checkBlockPos)) {
                hasWalls = false;
                break;
            }
        }
        if (hasWalls) {
            for (BlockPos offset : BLOCK_OFFSETS_TOP) {
                checkBlockPos = roomPos.blockPosition(offset);
                blockState = player.level.getBlockState(checkBlockPos);
                if (blockState.isAir(player.level, checkBlockPos) && player.level.getBlockState(checkBlockPos.above()).getBlock() != ModBlocks.MR_PRESIDENT_EXIT.get()) {
                    hasWalls = false;
                    break;
                }
            }
        }
        if (hasWalls) {
            ModCriteriaTriggers.MR_PRESIDENT_ROOM_WALLS.get().trigger(player);
        }
    }
}
