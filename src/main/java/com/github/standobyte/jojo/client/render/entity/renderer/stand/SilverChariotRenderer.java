package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.SilverChariotArmorLayer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.SilverChariotRapierFlameLayer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SilverChariotRenderer extends AbstractStandRenderer<SilverChariotEntity, SilverChariotModel> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/silver_chariot.png");

    public SilverChariotRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SilverChariotModel(), 0);
        addLayer(new SilverChariotArmorLayer(this));
        addLayer(new SilverChariotRapierFlameLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(SilverChariotEntity entity) {
        return TEXTURE;
    }
}
