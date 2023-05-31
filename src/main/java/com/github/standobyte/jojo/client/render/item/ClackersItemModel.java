package com.github.standobyte.jojo.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ClackersItemModel extends Model {
    private final ModelRenderer clackers;
    private final ModelRenderer string1;
    private final ModelRenderer ball1;
    private final ModelRenderer string2;
    private final ModelRenderer ball2;

    public ClackersItemModel() {
        super(RenderType::entityCutoutNoCull);
        texWidth = 32;
        texHeight = 32;

        clackers = new ModelRenderer(this);
        clackers.setPos(0.0F, -2.0F, 0.0F);
        clackers.texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, -0.25F, false);

        string1 = new ModelRenderer(this);
        string1.setPos(0.0F, 0.0F, 0.0F);
        clackers.addChild(string1);
        string1.texOffs(24, 6).addBox(-0.5F, -7.925F, -0.5F, 1.0F, 8.0F, 1.0F, -0.075F, false);

        ball1 = new ModelRenderer(this);
        ball1.setPos(0.0F, -7.85F, 0.0F);
        string1.addChild(ball1);
        ball1.texOffs(0, 0).addBox(-3.0F, -4.5F, -3.0F, 6.0F, 6.0F, 6.0F, -1.5F, false);

        string2 = new ModelRenderer(this);
        string2.setPos(0.0F, 0.0F, 0.0F);
        clackers.addChild(string2);
        string2.texOffs(28, 6).addBox(-0.5F, -0.075F, -0.5F, 1.0F, 8.0F, 1.0F, -0.075F, false);

        ball2 = new ModelRenderer(this);
        ball2.setPos(0.0F, 7.55F, 0.0F);
        string2.addChild(ball2);
        ball2.texOffs(0, 12).addBox(-3.0F, -1.2F, -3.0F, 6.0F, 6.0F, 6.0F, -1.5F, false);
    }

    public void setStringAngles(float xRot1, float yRot1, float zRot1, float xRot2, float yRot2, float zRot2) {
        string1.xRot = xRot1;
        string1.yRot = yRot1;
        string1.zRot = zRot1;
        string2.xRot = xRot2;
        string2.yRot = yRot2;
        string2.zRot = zRot2;
    }
    
    public ModelRenderer getMainPart() {
        return clackers;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        clackers.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
