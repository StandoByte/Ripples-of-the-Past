package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class DivineSandstormEffectLayer<T extends LivingEntity> extends LayerRenderer<T, PlayerModel<T>> { // TODO riptide effects on arms
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
    private final ModelRenderer box = new ModelRenderer(64, 64, 0, 0);

    public DivineSandstormEffectLayer(IEntityRenderer<T, PlayerModel<T>> renderer) {
        super(renderer);
        box.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F);
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        /*if (INonStandPower.getNonStandPowerOptional(entity)
                .map(power -> power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_DIVINE_SANDSTORM.get())
                .orElse(false)) {
            IVertexBuilder ivertexbuilder = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            for (int i = 0; i < 3; ++i) {
                matrixStack.pushPose();
                float f = ticks * (float)(-(45 + i * 5));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
                float f1 = 0.75F * (float)i;
                matrixStack.scale(f1, f1, f1);
                matrixStack.translate(0.0D, (double)(-0.2F + 0.6F * (float)i), 0.0D);
                this.box.render(matrixStack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY);
                matrixStack.popPose();
            }
        }*/
    }
}
