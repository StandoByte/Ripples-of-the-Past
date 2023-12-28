package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;

import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotRapierFlameLayerModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class SilverChariotRapierFlameLayer extends StandModelLayerRenderer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> {

    public SilverChariotRapierFlameLayer(SilverChariotRenderer entityRenderer) {
        super(entityRenderer, new SilverChariotRapierFlameLayerModel(), null);
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return LightTexture.pack(15, 15);
    }

    @Deprecated
    @Override
    public ResourceLocation getLayerTexture(Optional<ResourceLocation> standSkin) {
        return PlayerContainer.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(SilverChariotEntity entity, Optional<ResourceLocation> standSkin) {
        return entity != null && entity.isRapierOnFire();
    }
}
