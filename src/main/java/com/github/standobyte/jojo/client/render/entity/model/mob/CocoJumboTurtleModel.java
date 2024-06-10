package com.github.standobyte.jojo.client.render.entity.model.mob;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.TurtleModel;
import net.minecraft.entity.passive.TurtleEntity;

public class CocoJumboTurtleModel<T extends TurtleEntity> extends TurtleModel<T> {
    public boolean hasKey;

    public CocoJumboTurtleModel(float inflate) {
        super(inflate);
    }

    @Override
    public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, 
            int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        if (hasKey) {
            pBlue = 0;
        }
        else {
            pRed = 0.25f;
            pGreen = 0.25f;
            pBlue = 0.25f;
        }
        super.renderToBuffer(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

}
