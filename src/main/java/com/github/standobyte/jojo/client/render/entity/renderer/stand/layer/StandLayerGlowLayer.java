package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;

public class StandLayerGlowLayer<T extends StandEntity, M extends StandEntityModel<T>> extends StandGlowLayer<T, M> {
    private final StandModelLayerRenderer<T, M> layerRenderer;

    public StandLayerGlowLayer(StandEntityRenderer<T, M> entityRenderer, 
            StandModelLayerRenderer<T, M> layerRenderer) {
        super(entityRenderer, layerRenderer.getLayerTexture());
        this.layerRenderer = layerRenderer;
    }
    
    @Override
    public boolean shouldRender(T entity) {
        return super.shouldRender(entity) && layerRenderer.shouldRender(entity);
    }

    @Override
    public M getLayerModel() {
        return layerRenderer.getLayerModel();
    }
}
