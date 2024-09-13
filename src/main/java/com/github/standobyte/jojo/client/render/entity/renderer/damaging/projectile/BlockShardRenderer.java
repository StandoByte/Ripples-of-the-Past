package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.BlockShardEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class BlockShardRenderer extends EntityRenderer<BlockShardEntity> {

    public BlockShardRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockShardEntity pEntity) {
        BlockState block = pEntity.getBlock();
        if (block != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(block);
            if (sprite != null) {
                return CDBlockBulletRenderer.getSpriteTexture(sprite).orElse(PlayerContainer.BLOCK_ATLAS);
            }
        }
        return PlayerContainer.BLOCK_ATLAS;
    }
    
    @Override
    public void render(BlockShardEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
            BlockState blockState = entity.getBlock();
            if (blockState != null) {
                matrixStack.pushPose();
                matrixStack.scale(-1, -1, 1);
                ModelRenderer model = getModelRenderer(entity);
                rotate(entity, model, partialTick);
                
                ResourceLocation texture = getTextureLocation(entity);
                if (texture != PlayerContainer.BLOCK_ATLAS) {
                    RenderType renderType = RenderType.entitySolid(texture);
                    IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);
                    model.render(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY);
                    super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
                }
                matrixStack.popPose();
            }
        }
    }
    
    private ModelRenderer[] models = new ModelRenderer[64];
    private ModelRenderer getModelRenderer(BlockShardEntity entity) {
        long randomNum = entity.getUUID().getMostSignificantBits();
        int x = (int) (randomNum & 3);
        randomNum >>= 2;
        int y = (int) (randomNum & 3);
        randomNum >>= 2;
        int z = (int) (randomNum & 3);
        int index = x * 16 + y * 4 + z;
        
        if (models[index] == null) {
            ModelRenderer model = new ModelRenderer(16, 16, 0, 0);
            float xSize = x + 5;
            float ySize = y + 5;
            float zSize = z + 5;
            model.addBox(-xSize / 2, -ySize / 2, -zSize / 2, xSize, ySize, zSize);
            model.y = -3;
            models[index] = model;
        }
        return models[index];
    }
    
    private void rotate(BlockShardEntity entity, ModelRenderer modelRenderer, float partialTick) {
        long randomNum = entity.getUUID().getMostSignificantBits() >> 6;
        modelRenderer.xRot = (float) Math.PI * (int) (randomNum & 127) / 64;
        randomNum >>= 7;
        modelRenderer.yRot = (float) Math.PI * (int) (randomNum & 127) / 64;
        randomNum >>= 7;
        modelRenderer.zRot = (float) Math.PI * (int) (randomNum & 127) / 64;
        
//        float ticks = entity.tickCount + partialTick;
//        modelRenderer.xRot += ticks * 0.125f;
//        modelRenderer.yRot += ticks * 0.125f;
//        modelRenderer.zRot += ticks * 0.5f;
    }

}
