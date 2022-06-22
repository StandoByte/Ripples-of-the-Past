package com.github.standobyte.jojo.client.renderer.entity.stand.layer;

import com.github.standobyte.jojo.client.model.entity.stand.MagiciansRedFlameLayerModel;
import com.github.standobyte.jojo.client.model.entity.stand.MagiciansRedModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.MagiciansRedRenderer;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class MagiciansRedFlameLayer extends StandModelLayerRenderer<MagiciansRedEntity, MagiciansRedModel> {

    public MagiciansRedFlameLayer(MagiciansRedRenderer entityRenderer) {
        super(entityRenderer, new MagiciansRedFlameLayerModel());
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
    public boolean shouldRender(MagiciansRedEntity entity) {
        return !entity.isInWaterOrRain();
    }
}
