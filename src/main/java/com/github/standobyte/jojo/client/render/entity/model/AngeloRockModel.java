package com.github.standobyte.jojo.client.render.entity.model;

import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class AngeloRockModel extends EntityModel<AngeloRockEntity> {
    private final ModelRenderer rock;

    public AngeloRockModel() {
        texWidth = 16;
        texHeight = 16;

        rock = new ModelRenderer(this);
        rock.setPos(0.0F, 24.0F, 0.0F);
        
        rock.texOffs(-10, -5).addBox(-8.0F, -32.0F, -8.0F, 16.0F, 16.0F, 16.0F, 0.0F, false);
        rock.texOffs(-10, -5).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, 0.0F, false);
    }
    
    @Override
    public void setupAnim(AngeloRockEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        rock.yRot = pNetHeadYaw * MathUtil.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        matrixStack.pushPose();
        matrixStack.translate(0, -1.5, 0);
        rock.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        matrixStack.popPose();
    }

}
