package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.PillarmanTempleEngravingEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class PillarmanTempleEngravingRenderer extends EntityRenderer<PillarmanTempleEngravingEntity> {
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_1.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_2.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_3.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_4.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_5.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_6.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_7.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/engraving/engraving_8.png")
    };

    public PillarmanTempleEngravingRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(PillarmanTempleEngravingEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - yRotation));
        float f = 0.0625F;
        matrixStack.scale(f, f, f);
        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        renderPainting(matrixStack, vertexBuilder, entity);
        matrixStack.popPose();
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PillarmanTempleEngravingEntity entity) {
        int i = entity.getTextureId();
        if (i > TEXTURES.length || i < 0) {
            i = 0;
        }
        return TEXTURES[i];
    }

    // it just works
    private void renderPainting(MatrixStack matrixStack, IVertexBuilder vertexBuilder, PillarmanTempleEngravingEntity entity) {
        int width = entity.getWidth();
        int height = entity.getHeight();
        MatrixStack.Entry matrixStackEntry = matrixStack.last();
        Matrix4f matrix4f = matrixStackEntry.pose();
        Matrix3f matrixNormals = matrixStackEntry.normal();
        float f = (float)(-width) / 2.0F;
        float f1 = (float)(-height) / 2.0F;
        int widthBlocks = width / 16;
        int heightBlocks = height / 16;
        double d0 = 16.0D / (double)widthBlocks;
        double d1 = 16.0D / (double)heightBlocks;

        for (int blockX = 0; blockX < widthBlocks; ++blockX) {
            for (int blockY = 0; blockY < heightBlocks; ++blockY) {
                float f15 = f + (float)((blockX + 1) * 16);
                float f16 = f + (float)(blockX * 16);
                float f17 = f1 + (float)((blockY + 1) * 16);
                float f18 = f1 + (float)(blockY * 16);
                int entityX = MathHelper.floor(entity.getX());
                int entityY = MathHelper.floor(entity.getY() + (double)((f17 + f18) / 2.0F / 16.0F));
                int entityZ = MathHelper.floor(entity.getZ());
                Direction direction = entity.getDirection();
                if (direction == Direction.NORTH) {
                    entityX = MathHelper.floor(entity.getX() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.WEST) {
                    entityZ = MathHelper.floor(entity.getZ() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.SOUTH) {
                    entityX = MathHelper.floor(entity.getX() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.EAST) {
                    entityZ = MathHelper.floor(entity.getZ() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                int light = WorldRenderer.getLightColor(entity.level, new BlockPos(entityX, entityY, entityZ));

                float f19 = (float) (0.0625 * (d0 * (double)(widthBlocks - blockX)));
                float f20 = (float) (0.0625 * (d0 * (double)(widthBlocks - (blockX + 1))));
                float f21 = (float) (0.0625 * (d1 * (double)(heightBlocks - blockY)));
                float f22 = (float) (0.0625 * (d1 * (double)(heightBlocks - (blockY + 1))));
                this.vertex(matrix4f, matrixNormals, vertexBuilder, f15, f18, f20, f21, -0.5F, 0, 0, -1, light);
                this.vertex(matrix4f, matrixNormals, vertexBuilder, f16, f18, f19, f21, -0.5F, 0, 0, -1, light);
                this.vertex(matrix4f, matrixNormals, vertexBuilder, f16, f17, f19, f22, -0.5F, 0, 0, -1, light);
                this.vertex(matrix4f, matrixNormals, vertexBuilder, f15, f17, f20, f22, -0.5F, 0, 0, -1, light);
            }
        }

    }

    private void vertex(Matrix4f matrix, Matrix3f normals, IVertexBuilder vertexBuilder, 
            float offsetX, float offsetY, float textureX, float textureY, float offsetZ, 
            float normalX, float normalY, float normalZ, int packedLight) {
        vertexBuilder
        .vertex(matrix, offsetX, offsetY, offsetZ)
        .color(191, 191, 191, 191)
        .uv(textureX, textureY)
        .overlayCoords(OverlayTexture.NO_OVERLAY)
        .uv2(packedLight)
        .normal(normals, (float)normalX, (float)normalY, (float)normalZ)
        .endVertex();
    }
}
