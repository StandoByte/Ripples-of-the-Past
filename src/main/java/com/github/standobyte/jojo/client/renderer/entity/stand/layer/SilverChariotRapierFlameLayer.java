package com.github.standobyte.jojo.client.renderer.entity.stand.layer;

import com.github.standobyte.jojo.client.model.entity.stand.SilverChariotModel;
import com.github.standobyte.jojo.client.model.entity.stand.SilverChariotRapierFlameLayerModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class SilverChariotRapierFlameLayer extends StandModelLayerRenderer<SilverChariotEntity, SilverChariotModel> {

    public SilverChariotRapierFlameLayer(SilverChariotRenderer entityRenderer) {
        super(entityRenderer, new SilverChariotRapierFlameLayerModel());
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return LightTexture.pack(15, 15);
    }

    @Deprecated
    @Override
    protected ResourceLocation getLayerTexture() {
        return PlayerContainer.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(SilverChariotEntity entity) {
        return entity.isRapierOnFire();
    }
}
