package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.ResourceLocation;

public class StandLayerGlowLayer<T extends StandEntity, M extends StandEntityModel<T>> extends StandGlowLayer<T, M> {
    private final StandModelLayerRenderer<T, M> layerRenderer;

    public StandLayerGlowLayer(StandEntityRenderer<T, M> entityRenderer, 
            StandModelLayerRenderer<T, M> layerRenderer) {
        super(entityRenderer, layerRenderer.getBaseTexture());
        this.layerRenderer = layerRenderer;
    }
    
    @Override
    public boolean shouldRender(T entity, Optional<ResourceLocation> standSkin) {
        return super.shouldRender(entity, standSkin) && layerRenderer.shouldRender(entity, standSkin);
    }

    @Override
    public M getLayerModel(T entity) {
        return layerRenderer.getLayerModel(entity);
    }
}
