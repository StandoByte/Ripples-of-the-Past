package com.github.standobyte.jojo.client.model.pose;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;

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
        matrixStack.translate(x / 16.0, y / 16.0, z / 16.0);
        if (xRotSecond != 0.0F) {
            matrixStack.mulPose(Vector3f.XP.rotation(xRotSecond));
        }
        
        if (zRot != 0.0F) {
            matrixStack.mulPose(Vector3f.ZP.rotation(zRot));
        }

        if (yRot != 0.0F) {
            matrixStack.mulPose(Vector3f.YP.rotation(yRot));
        }

        if (xRot != 0.0F) {
            matrixStack.mulPose(Vector3f.XP.rotation(xRot));
        }

    }
}
