package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.model.projectile.CDBlockBulletModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBlockBulletEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CDBlockBulletRenderer extends SimpleEntityRenderer<CDBlockBulletEntity, CDBlockBulletModel> {

    public CDBlockBulletRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CDBlockBulletModel(), null);
    }
    
    @Override
    public ResourceLocation getTextureLocation(CDBlockBulletEntity entity) {
        ResourceLocation texture = entity.getBlockTex();
        if (texture == null) {
            texture = getBlockTexture(entity);
            entity.setBlockTex(texture);
        }
        return texture;
    }
    
    private static final Random RANDOM = new Random();
    private static final ResourceLocation GLASS_TEXTURE = new ResourceLocation("textures/block/glass.png");
    private ResourceLocation getBlockTexture(CDBlockBulletEntity entity) {
        if (entity.getBlock() != null) {
            ResourceLocation texture = getBlockTexture(entity.getBlock().defaultBlockState());
            return texture != null ? texture : GLASS_TEXTURE;
        }
        return GLASS_TEXTURE;
    }
    
    @Nullable
    public static ResourceLocation getBlockTexture(BlockState blockState) {
        IBakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
        List<BakedQuad> quads = blockModel.getQuads(blockState, Direction.NORTH, RANDOM, EmptyModelData.INSTANCE);
        if (!quads.isEmpty()) {
            TextureAtlasSprite sprite = quads.get(0).getSprite();
            if (sprite != null) {
                ResourceLocation name = sprite.getName();
                if (name != null) {
                    return new ResourceLocation(name.getNamespace(), "textures/" + name.getPath() + ".png");
                }
            }
        }
        return null;
    }
}
