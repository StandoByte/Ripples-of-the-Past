package com.github.standobyte.jojo.client.render.item.tommygun;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class TommyGunModel extends Model {
    private ModelRenderer tommyGun;
    private ModelRenderer fire;

    public TommyGunModel() {
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (tommyGun != null) {
            tommyGun.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
    
    public void renderFire(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (fire != null) {
            fire.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

}
