package com.github.standobyte.jojo.client;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks.PrevBlockInfo;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class TranslucentBlockRenderHelper {

    public static void renderTranslucentBlocks(MatrixStack matrixStack, Minecraft mc, 
            Stream<Map.Entry<BlockPos, PrevBlockInfo>> blocks, Predicate<BlockPos> inAbilityRange) {
    }
}
