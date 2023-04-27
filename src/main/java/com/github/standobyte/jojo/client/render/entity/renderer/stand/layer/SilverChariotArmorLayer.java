package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotArmorLayerModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.util.ResourceLocation;

public class SilverChariotArmorLayer extends StandModelLayerRenderer<SilverChariotEntity, SilverChariotModel> {
    private final ResourceLocation texture;

    public SilverChariotArmorLayer(SilverChariotRenderer entityRenderer, ResourceLocation texture) {
        super(entityRenderer, new SilverChariotArmorLayerModel());
        this.texture = texture;
    }
    
    public boolean shouldRender(SilverChariotEntity entity) {
        return entity.hasArmor();
    }

    @Override
    public ResourceLocation getLayerTexture() {
        return texture;
    }
}
