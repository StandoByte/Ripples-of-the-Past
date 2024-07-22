package com.github.standobyte.jojo.client.render.item.polaroid;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class PolaroidModel extends Model {
    private ModelRenderer polaroid;
    private ModelRenderer flash;
    private ModelRenderer photo;

    public PolaroidModel() {
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (polaroid != null) {
            polaroid.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (photo != null) {
            photo.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    
    public void setRenderPhoto(boolean renderPhoto) {
        if (polaroid != null) {
            polaroid.visible = !renderPhoto;
        }
        if (photo != null) {
            photo.visible = renderPhoto;
        }
    }
    
    public void setAnim(boolean open, float photoProgress) {
        if (flash != null) {
            flash.xRot = open ? (float) -Math.PI / 2 : 0;
        }
        
        if (photo != null) {
            photo.z = 1.25f - photoProgress * 8;
            
            float rot = photoProgress < 0.6f ? 0 : (photoProgress - 0.6f) * 2.5f;
            photo.xRot = 10 * MathUtil.DEG_TO_RAD * rot;
        }
    }
}
