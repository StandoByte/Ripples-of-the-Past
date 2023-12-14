package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.util.ResourceLocation;

public class SilverChariotArmorLayer extends StandModelLayerRenderer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> {
    private final ResourceLocation texture;

    public SilverChariotArmorLayer(SilverChariotRenderer entityRenderer, StandEntityModel<SilverChariotEntity> armorModel, ResourceLocation texture) {
        super(entityRenderer, armorModel);
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
