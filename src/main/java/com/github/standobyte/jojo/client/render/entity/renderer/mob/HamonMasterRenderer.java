package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.HamonMasterExtraLayer;
import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HamonMasterRenderer extends BipedRenderer<HamonMasterEntity, HamonMasterModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/hamon_master.png");

    public HamonMasterRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HamonMasterModel(false), 0.5F);
        addLayer(new HamonMasterExtraLayer(this, new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/hamon_master_extra.png")));
    }
    
    @Override
    public ResourceLocation getTextureLocation(HamonMasterEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(HamonMasterEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        model.initPose();
        model.setupPoseRotations(pMatrixStack, pPartialTicks);
    }
}
