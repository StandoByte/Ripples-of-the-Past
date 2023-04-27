package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.CrazyDiamondModel;
import com.github.standobyte.jojo.entity.stand.stands.CrazyDiamondEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class CrazyDiamondRenderer extends AbstractStandRenderer<CrazyDiamondEntity, CrazyDiamondModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/crazy_diamond.png");

    public CrazyDiamondRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CrazyDiamondModel(), 0);
    }

    @Override
    public ResourceLocation getTextureLocation(CrazyDiamondEntity entity) {
        return TEXTURE;
    }
}
