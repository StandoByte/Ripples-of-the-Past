package com.github.standobyte.jojo.client.renderer.entity.stand.layer;

import com.github.standobyte.jojo.client.model.entity.stand.MagiciansRedFlameLayerModel;
import com.github.standobyte.jojo.client.model.entity.stand.MagiciansRedModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.MagiciansRedRenderer;
import com.github.standobyte.jojo.entity.stand.MagiciansRedEntity;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.ResourceLocation;

public class MagiciansRedFlameLayer extends StandModelLayerRenderer<MagiciansRedEntity, MagiciansRedModel> {
    private final MagiciansRedFlameLayerModel model = new MagiciansRedFlameLayerModel();

    public MagiciansRedFlameLayer(MagiciansRedRenderer entityRenderer) {
        super(entityRenderer);
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return LightTexture.pack(15, 15);
    }
    
    @Override
    public MagiciansRedFlameLayerModel getLayerModel() {
        return model;
    }
    
    @Override
    public IVertexBuilder getBuffer(IRenderTypeBuffer buffer, MagiciansRedEntity entity) {
        return buffer.getBuffer(Atlases.translucentCullBlockSheet());
    }

    @Deprecated
    @Override
    protected ResourceLocation getLayerTexture() {
        return null;
    }

    @Override
    public boolean shouldRender(MagiciansRedEntity entity) {
        return !entity.isInWaterOrRain();
    }
}
