package com.github.standobyte.jojo.client.renderer.entity.stand.layer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.model.entity.stand.HierophantGreenModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.HierophantGreenRenderer;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;

import net.minecraft.util.ResourceLocation;

public class HierophantGreenGlowLayer extends StandModelLayerRenderer<HierophantGreenEntity, HierophantGreenModel> {
    private static final ResourceLocation LAYER_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/hierophant_green_glow.png");

    public HierophantGreenGlowLayer(HierophantGreenRenderer entityRenderer) {
        super(entityRenderer, new HierophantGreenModel());
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return ClientUtil.MAX_MODEL_LIGHT;
    }

    @Override
    public ResourceLocation getLayerTexture() {
        return LAYER_TEXTURE;
    }
}
