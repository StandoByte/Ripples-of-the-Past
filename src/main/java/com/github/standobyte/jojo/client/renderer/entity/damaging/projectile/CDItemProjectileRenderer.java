package com.github.standobyte.jojo.client.renderer.entity.damaging.projectile;

import java.util.List;
import java.util.Random;

import com.github.standobyte.jojo.client.model.entity.projectile.CDItemProjectileModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.CDItemProjectileEntity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

// FIXME ! (item projectile) render a blood drop if it's homing
public class CDItemProjectileRenderer extends SimpleEntityRenderer<CDItemProjectileEntity, CDItemProjectileModel> {

    public CDItemProjectileRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CDItemProjectileModel(), null);
    }
    
    @Override
    public ResourceLocation getTextureLocation(CDItemProjectileEntity entity) {
        ResourceLocation texture = entity.getBlockTex();
        if (texture == null) {
            texture = getBlockTexture(entity);
            entity.setBlockTex(texture);
        }
        return texture;
    }
    
    private static final Random RANDOM = new Random();
    private static final ResourceLocation GLASS_TEXTURE = new ResourceLocation("textures/block/glass.png");
    private ResourceLocation getBlockTexture(CDItemProjectileEntity entity) {
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
