package com.github.standobyte.jojo.client.renderer.entity.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.stand.MagiciansRedModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.layer.MagiciansRedFlameLayer;
import com.github.standobyte.jojo.entity.stand.MagiciansRedEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class MagiciansRedRenderer extends AbstractStandRenderer<MagiciansRedEntity, MagiciansRedModel> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/magicians_red.png");

    public MagiciansRedRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MagiciansRedModel(), 0);
        addLayer(new MagiciansRedFlameLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MagiciansRedEntity entity) {
        return TEXTURE;
    }
}
