package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.ResourceLocation;

public class StandGlowLayer<T extends StandEntity, M extends StandEntityModel<T>> extends StandModelLayerRenderer<T, M> {
    private static final Set<StandGlowLayer<?, ?>> LAYERS = new HashSet<>();
    
    private final ResourceLocation layerTexture;
    @Nullable
    private ResourceLocation legacyTexture = null;
    private boolean texPresent;
    private boolean recheckTexPath = true;
    private boolean useLegacyTex;

    public StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, ResourceLocation baseTex) {
        this(entityRenderer, entityRenderer.getModel(), baseTex);
    }

    protected StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, M model, ResourceLocation baseTex) {
        super(entityRenderer, model);
        layerTexture = new ResourceLocation(
                baseTex.getNamespace(), 
                baseTex.getPath().replace("/entity/stand", "/entity/stand/glow"));
        LAYERS.add(this);
    }
    
    public void setLegacyTexture(ResourceLocation legacyTex) {
        this.legacyTexture = legacyTex;
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return ClientUtil.MAX_MODEL_LIGHT;
    }
    
    @Override
    public ResourceLocation getLayerTexture() {
        return useLegacyTex ? legacyTexture : layerTexture;
    }
    
    @Override
    public boolean shouldRender(T entity) {
        if (recheckTexPath) {
            recheckTexPath = false;
            texPresent = false;
            useLegacyTex = false;
            
            if (legacyTexture != null) {
                texPresent = ClientUtil.resourceExists(legacyTexture);
                if (texPresent) {
                    useLegacyTex = true;
                }
            }
            
            if (!texPresent) {
                texPresent = ClientUtil.resourceExists(layerTexture);
            }
        }
        return texPresent;
    }
    
    public void setNeedsTexRecheck() {
        this.recheckTexPath = true;
    }
    
    
    
    public static Collection<StandGlowLayer<?, ?>> allGlowLayers() {
        return Collections.unmodifiableSet(LAYERS);
    }
}
