package com.github.standobyte.jojo.client.renderer.entity.mob;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;

import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HamonMasterRenderer extends BipedRenderer<HamonMasterEntity, HamonMasterModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/hamon_master.png");

    public HamonMasterRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HamonMasterModel(), 0.5F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(HamonMasterEntity entity) {
        return TEXTURE;
    }

}
