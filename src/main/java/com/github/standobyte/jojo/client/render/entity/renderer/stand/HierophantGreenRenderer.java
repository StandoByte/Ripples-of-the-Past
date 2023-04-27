package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.HierophantGreenModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.HierophantGreenGlowLayer;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HierophantGreenRenderer extends AbstractStandRenderer<HierophantGreenEntity, HierophantGreenModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/hierophant_green.png");

    public HierophantGreenRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HierophantGreenModel(), 0);
        addLayer(new HierophantGreenGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(HierophantGreenEntity entity) {
        return TEXTURE;
    }
}
