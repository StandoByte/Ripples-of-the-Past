package com.github.standobyte.jojo.client.renderer.entity.damaging.projectile;

import java.util.List;
import java.util.Random;

import com.github.standobyte.jojo.client.model.entity.projectile.CDBlockBulletModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBlockBulletEntity;

import net.minecraft.block.Block;
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
        IBakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState());
        List<BakedQuad> quads = blockModel.getQuads(block.defaultBlockState(), Direction.NORTH, RANDOM, EmptyModelData.INSTANCE);
        if (!quads.isEmpty()) {
            TextureAtlasSprite sprite = quads.get(0).getSprite();
            if (sprite != null) {
                ResourceLocation name = sprite.getName();
                if (name != null) {
                    return new ResourceLocation(name.getNamespace(), "textures/" + name.getPath() + ".png");
                }
            }
        }
        return GLASS_TEXTURE;
    }

}
