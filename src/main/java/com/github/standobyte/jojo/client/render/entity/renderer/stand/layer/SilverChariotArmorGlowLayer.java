package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.util.ResourceLocation;

public class SilverChariotArmorGlowLayer extends StandGlowLayer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> {

    public SilverChariotArmorGlowLayer(StandEntityRenderer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> entityRenderer, 
            SilverChariotArmorLayer armorLayer) {
        super(entityRenderer, armorLayer.getLayerModel(), armorLayer.getBaseTexture());
    }
    
    @Override
    public boolean shouldRender(SilverChariotEntity entity, Optional<ResourceLocation> standSkin) {
        return super.shouldRender(entity, standSkin) && (entity == null || entity.hasArmor());
    }
}
