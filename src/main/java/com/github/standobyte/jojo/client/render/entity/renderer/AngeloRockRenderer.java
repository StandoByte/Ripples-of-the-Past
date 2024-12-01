package com.github.standobyte.jojo.client.render.entity.renderer;

import java.util.Map;

import com.github.standobyte.jojo.client.render.entity.model.AngeloRockModel;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBlockBulletRenderer;
import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class AngeloRockRenderer extends SimpleEntityRenderer<AngeloRockEntity, AngeloRockModel> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/block/stone.png");

    public AngeloRockRenderer(EntityRendererManager renderManager) {
        super(renderManager, new AngeloRockModel(), TEXTURE);
    }
    
    @Override
    protected void doRender(AngeloRockEntity entity, AngeloRockModel model, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        Map<BlockState, int[]> blocksForPieces = entity.getStonePiecesRender();
        if (blocksForPieces != null && !blocksForPieces.isEmpty()) {
            for (Map.Entry<BlockState, int[]> blockStateEntry : blocksForPieces.entrySet()) {
                ResourceLocation texture = CDBlockBulletRenderer.getTexture(blockStateEntry.getKey(), TEXTURE);
                IVertexBuilder vertexBuilder = buffer.getBuffer(model.renderType(texture));
                model.setPiecesVisibility(blockStateEntry.getValue());
                model.setCreationAnim(entity, entity.getCreationAnimProgress(partialTick));
                model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
    
}
