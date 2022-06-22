package com.github.standobyte.jojo.client.renderer.entity.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.model.entity.projectile.SCRapierFlameModel;
import com.github.standobyte.jojo.client.model.entity.projectile.SCRapierModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class SCRapierRenderer extends SimpleEntityRenderer<SCRapierEntity, SCRapierModel> {
    private final SCRapierFlameModel flameModel;

    public SCRapierRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SCRapierModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/silver_chariot.png"));
        this.flameModel = new SCRapierFlameModel();
    }
    
    protected void rotateModel(SCRapierModel model, SCRapierEntity entity, float partialTick, float yRotation, float xRotation, MatrixStack matrixStack) {
    	super.rotateModel(model, entity, partialTick, yRotation, xRotation, matrixStack);
    	flameModel.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    @Override
    protected void doRender(SCRapierEntity entity, SCRapierModel model, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
    	super.doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
        if (entity.isOnFire()) {
        	flameModel.renderToBuffer(matrixStack, buffer.getBuffer(flameModel.renderType(null)), 
        			ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

}
