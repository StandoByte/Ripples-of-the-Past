package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

// TODO render the layers in 1st person when the player is invisible
public interface IFirstPersonHandLayer {
    void renderHandFirstPerson(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, 
            PlayerRenderer playerRenderer);
    
    static void defaultRender(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, 
            PlayerRenderer playerRenderer, 
            PlayerModel<AbstractClientPlayerEntity> model, ResourceLocation texture) {
        defaultRender(side, matrixStack, buffer, light, player, playerRenderer, model, texture,
                1, 1, 1, 1);
    }
    
    static void defaultRender(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, 
            PlayerRenderer playerRenderer, 
            PlayerModel<AbstractClientPlayerEntity> model, ResourceLocation texture,
            float red, float green, float blue, float alpha) {
        if (texture == null || player.isSpectator()) return;
        ClientUtil.setupForFirstPersonRender(model, player);
        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
        ModelRenderer arm = ClientUtil.getArm(model, side);
        ModelRenderer armOuter = ClientUtil.getArmOuter(model, side);
        arm.xRot = 0.0F;
        arm.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        armOuter.xRot = 0.0F;
        armOuter.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }
}
