package com.github.standobyte.jojo.client.render.item.generic;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

@SuppressWarnings("deprecation")
public class ItemISTERModelWrapper implements IBakedModel {
    private IBakedModel existingModel;
    private ISTERItemCaptureEntity captureEntityOverrides = null;
    
    public ItemISTERModelWrapper(IBakedModel existingModel) {
        this.existingModel = existingModel;
        setCaptureEntity();
    }
    
    public ItemISTERModelWrapper setCaptureEntity() {
        captureEntityOverrides = new ISTERItemCaptureEntity();
        return this;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @Nonnull Random rand) {
        return this.existingModel.getQuads(state, direction, rand);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.existingModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return this.existingModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.existingModel.getParticleIcon();
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return this.existingModel.getTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        if (captureEntityOverrides != null) {
            return captureEntityOverrides;
        }
        return this.existingModel.getOverrides();
    }

}
