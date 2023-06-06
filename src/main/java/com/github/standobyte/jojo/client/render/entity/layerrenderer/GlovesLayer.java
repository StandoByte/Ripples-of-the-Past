package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.item.GlovesItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GlovesLayer<T extends LivingEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {
    private final M glovesModel;
    private final boolean slim;
    private boolean playerAnimHandled = false;
    
    public GlovesLayer(IEntityRenderer<T, M> renderer, M glovesModel, boolean slim) {
        super(renderer);
        this.glovesModel = glovesModel;
        this.slim = slim;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        if (!playerAnimHandled) {
            PlayerAnimationHandler.getPlayerAnimator().onArmorLayerInit(this);
            playerAnimHandled = true;
        }
        
        // gloves
        ItemStack glovesItem = entity.getMainHandItem();
        if (!(!glovesItem.isEmpty() && glovesItem.getItem() instanceof GlovesItem)) {
            glovesItem = entity.getOffhandItem();
        }
        if (  !glovesItem.isEmpty() && glovesItem.getItem() instanceof GlovesItem) {
            GlovesItem gloves = (GlovesItem) glovesItem.getItem();
            M playerModel = getParentModel();
            playerModel.copyPropertiesTo(glovesModel);
            glovesModel.setupAnim(entity, limbSwing, limbSwingAmount, ticks, yRot, xRot);
            glovesModel.leftArm.visible = playerModel.leftArm.visible;
            glovesModel.leftSleeve.visible = playerModel.leftArm.visible;
            glovesModel.rightArm.visible = playerModel.rightArm.visible;
            glovesModel.rightSleeve.visible = playerModel.rightArm.visible;
            ResourceLocation texture = new ResourceLocation(
                    gloves.getRegistryName().getNamespace(), 
                    "textures/entity/biped/layer/" + gloves.getRegistryName().getPath() + (slim ? "_slim" : "") + ".png");
            IVertexBuilder vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, glovesItem.hasFoil());
            glovesModel.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        }
    }

}
