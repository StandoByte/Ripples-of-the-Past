package com.github.standobyte.jojo.client.renderer.entity.stand.layer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.stand.SilverChariotArmorLayerModel;
import com.github.standobyte.jojo.client.model.entity.stand.SilverChariotModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.util.ResourceLocation;

public class SilverChariotArmorLayer extends StandModelLayerRenderer<SilverChariotEntity, SilverChariotModel> {
    private static final ResourceLocation LAYER_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/silver_chariot_armor.png");

    public SilverChariotArmorLayer(SilverChariotRenderer entityRenderer) {
        super(entityRenderer, new SilverChariotArmorLayerModel());
    }
    
    public boolean shouldRender(SilverChariotEntity entity) {
        return entity.hasArmor();
    }

    @Override
    public ResourceLocation getLayerTexture() {
        return LAYER_TEXTURE;
    }
}
