package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.projectile.HamonCutterModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonCutterEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class HamonCutterRenderer extends SimpleEntityRenderer<HamonCutterEntity, HamonCutterModel> {

    public HamonCutterRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HamonCutterModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hamon_cutter.png"));
    }
    
    @Override
    protected void renderModel(HamonCutterEntity entity, HamonCutterModel model, float partialTick, 
            MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight) {
        float[] rgb = ClientUtil.rgb(entity.getColor());
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 
                rgb[0], rgb[1], rgb[2], 1.0F);
    }
}
