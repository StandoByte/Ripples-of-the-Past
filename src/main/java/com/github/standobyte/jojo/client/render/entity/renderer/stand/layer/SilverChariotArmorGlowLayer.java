package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

public class SilverChariotArmorGlowLayer extends StandGlowLayer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> {

    public SilverChariotArmorGlowLayer(StandEntityRenderer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> entityRenderer, 
            SilverChariotArmorLayer armorLayer) {
        super(entityRenderer, armorLayer.getLayerModel(), armorLayer.getLayerTexture());
    }
    
    @Override
    public boolean shouldRender(SilverChariotEntity entity) {
        return super.shouldRender(entity) && entity.hasArmor();
    }
}
