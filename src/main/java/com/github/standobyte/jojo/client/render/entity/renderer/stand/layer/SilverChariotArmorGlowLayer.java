package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

public class SilverChariotArmorGlowLayer extends StandGlowLayer<SilverChariotEntity, SilverChariotModel> {

    public SilverChariotArmorGlowLayer(StandEntityRenderer<SilverChariotEntity, SilverChariotModel> entityRenderer, SilverChariotArmorLayer armorLayer) {
        super(entityRenderer, armorLayer.getLayerModel(), armorLayer.getLayerTexture());
    }
    
    @Override
    public boolean shouldRender(SilverChariotEntity entity) {
        return entity.hasArmor();
    }
}
