package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotArmorLayerModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.SilverChariotArmorLayer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.SilverChariotRapierFlameLayer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.StandLayerGlowLayer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SilverChariotRenderer extends StandEntityRenderer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> {
    
    public SilverChariotRenderer(EntityRendererManager renderManager) {
        super(renderManager, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot"), SilverChariotModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/silver_chariot.png"), 0);
        SilverChariotArmorLayer armorLayer = new SilverChariotArmorLayer(this, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_armor"), SilverChariotArmorLayerModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/silver_chariot_armor.png"));
        addLayer(armorLayer);
        addLayer(new StandLayerGlowLayer<>(this, armorLayer));
        addLayer(new SilverChariotRapierFlameLayer(this));
    }
}
