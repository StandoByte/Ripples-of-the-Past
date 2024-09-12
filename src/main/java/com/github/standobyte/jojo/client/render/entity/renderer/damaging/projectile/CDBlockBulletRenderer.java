package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.github.standobyte.jojo.client.render.entity.model.projectile.CDBlockBulletModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBlockBulletEntity;

import net.minecraft.block.Block;
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
        Block block = entity.getBlock();
        if (block == null) {
            return GLASS_TEXTURE;
        }
        return getTexture(block.defaultBlockState(), GLASS_TEXTURE);
    }
    
    public static ResourceLocation getTexture(BlockState blockState, ResourceLocation defaultTex) {
        IBakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
        List<BakedQuad> quads = blockModel.getQuads(blockState, Direction.NORTH, RANDOM, EmptyModelData.INSTANCE);
        if (!quads.isEmpty()) {
            TextureAtlasSprite sprite = quads.get(0).getSprite();
            return getSpriteTexture(sprite).orElse(defaultTex);
        }
        return defaultTex;
    }
    
    public static Optional<ResourceLocation> getSpriteTexture(TextureAtlasSprite sprite) {
        if (sprite != null) {
            ResourceLocation name = sprite.getName();
            if (name != null) {
                return Optional.of(new ResourceLocation(name.getNamespace(), "textures/" + name.getPath() + ".png"));
            }
        }
        return Optional.empty();
    }
}
