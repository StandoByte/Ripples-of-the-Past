package com.github.standobyte.jojo.client;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

// A helper class for rendering translucent blocks overlay
// as a quality-of-life feature for Crazy Diamond's terrain restoration ability

// The original idea of remapping render types is taken from
// MultiblockVisualizationHandler class from Vazkii's Patchouli mod
// (licensed under CC BY-NC-SA 3.0)
public class TranslucentBlockRenderHelper {
    private static IRenderTypeBuffer.Impl buffers = null;

    public static void renderCDRestorationTranslucentBlocks(MatrixStack matrixStack, Minecraft mc, 
            Stream<PrevBlockInfo> blocks, Predicate<PrevBlockInfo> inAbilityRange) {
        if (buffers == null) {
            buffers = ModifiedRenderTypeBuffers.create(mc.renderBuffers().bufferSource(), TranslusencyRenderType::new);
        }
        ActiveRenderInfo renderInfo = mc.gameRenderer.getMainCamera();
        Vector3d projectedView = renderInfo.getPosition();
        matrixStack.pushPose();
        matrixStack.translate(
                -projectedView.x(), 
                -projectedView.y(), 
                -projectedView.z());

        BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
        int overlayTexture = OverlayTexture.pack(Math.abs((int) (Util.getMillis() % 2000) / 100 - 10), 10);
        blocks.forEach(block -> {
            BlockPos pos = block.pos;
            BlockState state = block.state;
            IModelData tileData = ModelDataManager.getModelData(mc.level, pos);
            if (tileData == null) tileData = EmptyModelData.INSTANCE;
            IModelData model = renderer.getBlockModel(state).getModelData(mc.level, pos, state, tileData);
            matrixStack.pushPose();
            matrixStack.translate(
                    pos.getX(), 
                    pos.getY(), 
                    pos.getZ());
            renderer.renderBlock(state, matrixStack, buffers, 0xF000F0, inAbilityRange.test(block) ? overlayTexture : OverlayTexture.NO_OVERLAY, model);
            matrixStack.popPose();
        });

        buffers.endBatch();
        matrixStack.popPose();
    }
}
