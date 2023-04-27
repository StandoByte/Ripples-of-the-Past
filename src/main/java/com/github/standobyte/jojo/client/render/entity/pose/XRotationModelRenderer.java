package com.github.standobyte.jojo.client.render.entity.pose;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class XRotationModelRenderer extends ModelRenderer {
    public float xRotSecond = 0;

    public XRotationModelRenderer(Model model) {
        super(model);
    }

    public XRotationModelRenderer(Model model, int xTexOffs, int yTexOffs) {
        super(model, xTexOffs, yTexOffs);
    }

    public XRotationModelRenderer(int xTexSize, int yTexSize, int xTexOffs, int yTexOffs) {
        super(xTexSize, yTexSize, xTexOffs, yTexOffs);
    }

    @Override
    public void translateAndRotate(MatrixStack matrixStack) {
        if (xRotSecond != 0.0F) {
            ClientUtil.rotateAngles(this, xRotSecond);
            xRotSecond = 0;
        }
        super.translateAndRotate(matrixStack);
    }
}
