package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class InkLipsLayer<T extends PlayerEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/layer/ink_lips.png");
    public InkLipsLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            T player, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        if (!player.isInvisible()) {
            int atePastaTicks = player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getInkPastaVisuals()).orElse(0);
            if (atePastaTicks > 0) {
                M model = getParentModel();
                float alpha = atePastaTicks > 200 ? 1 : (float) atePastaTicks / 200;
                IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
                model.renderToBuffer(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, LivingRenderer.getOverlayCoords(player, 0.0F), 1.0F, 1.0F, 1.0F, alpha);
            }
        }
    }
    
}
