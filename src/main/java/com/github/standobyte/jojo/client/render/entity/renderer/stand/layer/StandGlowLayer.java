package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.ResourceLocation;

public class StandGlowLayer<T extends StandEntity, M extends StandEntityModel<T>> extends StandModelLayerRenderer<T, M> {
    private final ResourcePathChecker texturePathCheck;

    public StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, ResourceLocation baseTex) {
        this(entityRenderer, null, baseTex);
    }

    protected StandGlowLayer(StandEntityRenderer<T, M> entityRenderer, M model, ResourceLocation baseTex) {
        super(entityRenderer, model == null, model, new ResourceLocation(
                baseTex.getNamespace(), 
                baseTex.getPath().replace("/entity/stand", "/entity/stand/glow")));
        this.texturePathCheck = ResourcePathChecker.getOrCreate(texture);
    }
    
    @Override
    public ResourceLocation getLayerTexture(Optional<ResourceLocation> standSkin) {
        return skinContainsGlow(standSkin).orElse(super.getLayerTexture(standSkin));
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return ClientUtil.MAX_MODEL_LIGHT;
    }
    
    private Optional<ResourceLocation> skinContainsGlow(Optional<ResourceLocation> standSkin) {
        if (!standSkin.isPresent()) {
            return Optional.empty();
        }
        return StandSkinsManager.getInstance()
                .getStandSkin(standSkin)
                .map(skin -> skin.getRemappedResPath(texture))
                .flatMap(resource -> resource.resourceExists() ? Optional.of(resource.getPath()) : Optional.empty());
    }
    
    
    
    @Override
    public boolean shouldRender(T entity, Optional<ResourceLocation> standSkin) {
        return texturePathCheck.resourceExists() || skinContainsGlow(standSkin).isPresent();
    }
}
