package com.github.standobyte.jojo.client;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;

//FIXME !!! (restore terrain) blocks overlay
public class TranslucentBlockRenderHelper {

    public static void renderTranslucentBlocks(MatrixStack matrixStack, Minecraft mc, 
            Stream<PrevBlockInfo> blocks, Predicate<PrevBlockInfo> inAbilityRange) {
    }
}
