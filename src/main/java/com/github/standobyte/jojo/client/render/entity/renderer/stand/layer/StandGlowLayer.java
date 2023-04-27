package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.ResourceLocation;

public class StandGlowLayer<T extends StandEntity, M extends StandEntityModel<T>> extends StandModelLayerRenderer<T, M> {
    private final ResourceLocation layerTexture;

    public StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, ResourceLocation baseTex) {
        this(entityRenderer, entityRenderer.getModel(), baseTex);
    }

    protected StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, M model, ResourceLocation baseTex) {
        super(entityRenderer, model);
        layerTexture = new ResourceLocation(
                baseTex.getNamespace(), 
                baseTex.getPath().replace("/entity/stand", "/entity/stand/glow"));
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return ClientUtil.MAX_MODEL_LIGHT;
    }

    @Override
    public ResourceLocation getLayerTexture() {
        return layerTexture;
    }
}
