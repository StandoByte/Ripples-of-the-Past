package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.ResourceLocation;

public class StandGlowLayer<T extends StandEntity, M extends StandEntityModel<T>> extends StandModelLayerRenderer<T, M> {
    private final ResourcePathChecker layerTexture;
    @Nullable
    private ResourcePathChecker legacyTexture;

    public StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, ResourceLocation baseTex) {
        this(entityRenderer, entityRenderer.getModel(), baseTex);
    }

    protected StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, M model, ResourceLocation baseTex) {
        super(entityRenderer, model);
        ResourceLocation layerTexture = new ResourceLocation(
                baseTex.getNamespace(), 
                baseTex.getPath().replace("/entity/stand", "/entity/stand/glow"));
        this.layerTexture = ResourcePathChecker.create(layerTexture);
    }
    
    public void setLegacyTexture(ResourceLocation legacyTex) {
        this.legacyTexture = legacyTex != null ? ResourcePathChecker.create(legacyTex) : null;
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return ClientUtil.MAX_MODEL_LIGHT;
    }
    
    @Override
    public ResourceLocation getLayerTexture() {
        if (legacyTexture != null && legacyTexture.resourceExists()) {
            return legacyTexture.getPath();
        }
        else {
            return layerTexture.getPath();
        }
    }
    
    @Override
    public boolean shouldRender(T entity) {
        return layerTexture.resourceExists() ||
                legacyTexture != null && legacyTexture.resourceExists();
    }
}
