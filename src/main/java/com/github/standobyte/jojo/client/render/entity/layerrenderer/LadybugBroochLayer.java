package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class LadybugBroochLayer<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {
    private final LadybugBroochesModel<T> broochesModel;
    
    public LadybugBroochLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
        this.broochesModel = new LadybugBroochesModel<>();
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(brooches -> {
            this.getParentModel().copyPropertiesTo(broochesModel);
            renderSingleBrooch(matrixStack, buffer, packedLight, entity, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot, 
                    brooches.getBroochWorn(0), broochesModel.broochLeft);
            renderSingleBrooch(matrixStack, buffer, packedLight, entity, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot, 
                    brooches.getBroochWorn(1), broochesModel.broochRight);
            renderSingleBrooch(matrixStack, buffer, packedLight, entity, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot, 
                    brooches.getBroochWorn(2), broochesModel.broochBottom);
        });
    }
    
    private void renderSingleBrooch(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot,
            DyeColor color, ModelRenderer broochPart) {
        if (color != null) {
            broochesModel.broochLeft.visible = false;
            broochesModel.broochRight.visible = false;
            broochesModel.broochBottom.visible = false;
            broochPart.visible = true;
            ResourceLocation texture = TEXTURE_BY_COLOR.get(color);
            IVertexBuilder vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, false);
            broochesModel.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    private static final Map<DyeColor, ResourceLocation> TEXTURE_BY_COLOR = Util.make(new EnumMap<>(DyeColor.class), map -> {
        for (DyeColor dye : DyeColor.values()) {
            map.put(dye, new ResourceLocation("jojo_clothes", "textures/misc/brooch/ladybug_" + dye.getName() + ".png"));
        }
    });
}
