package com.github.standobyte.jojo.client.render.entity.model;

import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class AngeloRockModel extends EntityModel<AngeloRockEntity> {
    private final ModelRenderer[] rockPieces;

    public AngeloRockModel() {
        texWidth = 16;
        texHeight = 16;
        
        rockPieces = new ModelRenderer[AngeloRockEntity.STONE_PIECES_COUNT];
        for (int i = 0; i < rockPieces.length; i++) {
            rockPieces[i] = new ModelRenderer(this);
            rockPieces[i].setPos(0.0F, 24.0F, 0.0F);
        }
        
        rockPieces[0] .texOffs(-11, -6).addBox(-8.0F, -32.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[1] .texOffs(-11, -6).addBox(0.0F, -32.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[2] .texOffs(-11, -6).addBox(-8.0F, -24.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[3] .texOffs(-11, -6).addBox(0.0F, -24.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[4] .texOffs(-11, -6).addBox(-8.0F, -16.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[5] .texOffs(-11, -6).addBox(0.0F, -16.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[6] .texOffs(-11, -6).addBox(-8.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[7] .texOffs(-11, -6).addBox(0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[8] .texOffs(-11, -6).addBox(-8.0F, -32.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[9] .texOffs(-11, -6).addBox(0.0F, -32.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[10].texOffs(-11, -6).addBox(-8.0F, -24.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[11].texOffs(-11, -6).addBox(0.0F, -24.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[12].texOffs(-11, -6).addBox(-8.0F, -16.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[13].texOffs(-11, -6).addBox(0.0F, -16.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[14].texOffs(-11, -6).addBox(-8.0F, -8.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        rockPieces[15].texOffs(-11, -6).addBox(0.0F, -8.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
    }
    
    @Override
    public void setupAnim(AngeloRockEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        for (ModelRenderer piece : rockPieces) {
            piece.yRot = pNetHeadYaw * MathUtil.DEG_TO_RAD;
        }
    }
    
    public void setPiecesVisibility(int[] pieces) {
        for (ModelRenderer piece : rockPieces) {
            piece.visible = false;
        }
        for (int i : pieces) {
            if (i >= 0 && i < rockPieces.length) {
                rockPieces[i].visible = true;
            }
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        matrixStack.pushPose();
        matrixStack.translate(0, -1.5, 0);
        for (ModelRenderer piece : rockPieces) {
            piece.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        matrixStack.popPose();
    }

}
