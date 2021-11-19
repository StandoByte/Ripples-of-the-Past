package com.github.standobyte.jojo.client.renderer.entity.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.stand.SilverChariotModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.layer.SilverChariotArmorLayer;
import com.github.standobyte.jojo.entity.stand.SilverChariotEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SilverChariotRenderer extends AbstractStandRenderer<SilverChariotEntity, SilverChariotModel> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/silver_chariot.png");

    public SilverChariotRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SilverChariotModel(), 0);
        addLayer(new SilverChariotArmorLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(SilverChariotEntity entity) {
        return TEXTURE;
    }
}
