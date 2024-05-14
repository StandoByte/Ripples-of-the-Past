package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.util.ResourceLocation;

public class SilverChariotArmorLayer extends StandModelLayerRenderer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> {

    public SilverChariotArmorLayer(SilverChariotRenderer entityRenderer, StandEntityModel<SilverChariotEntity> armorModel, ResourceLocation texture) {
        super(entityRenderer, armorModel, texture);
    }
    
    @Override
    public boolean shouldRender(SilverChariotEntity entity, Optional<ResourceLocation> standSkin) {
        return entity == null || entity.hasArmor();
    }
}
