package com.github.standobyte.jojo.client.render.entity.model.mob;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.bb.IllMakeItReadTheFileLater;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGenericModel;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.TurtleModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.TurtleEntity;

public class CocoJumboTurtleModel<T extends TurtleEntity> extends TurtleModel<T> {
    public boolean hasKey;
    private final ModelRenderer mrPresidentKey;
    
    public CocoJumboTurtleModel(float inflate) {
        super(inflate);

        ParseGenericModel.ModelParsed.ElementMesh meshParsed = ParseGenericModel.GSON.fromJson(IllMakeItReadTheFileLater.JSON, ParseGenericModel.ModelParsed.ElementMesh.class);
        ModelRenderer.ModelBox shellMesh = meshParsed.makeCube(new float[] { body.x, body.y + 2, body.z }, texWidth, texHeight);
        ClientReflection.getCubes(body).set(0, shellMesh);
        
        mrPresidentKey = new ModelRenderer(this);
        mrPresidentKey.setPos(0.0F, 11.0F, -10.0F);
        mrPresidentKey.texOffs(89, 1).addBox(-0.5F, 4.9F, 12.0F, 1.0F, 1.0F, 7.0F, -0.1F, false);
        mrPresidentKey.texOffs(105, 7).addBox(-0.5F, 4.9F, 13.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        mrPresidentKey.texOffs(98, 5).addBox(-1.25F, 4.9F, 16.0F, 1.0F, 1.0F, 2.0F, -0.15F, false);

        ModelRenderer mrPresidentKey_r1 = new ModelRenderer(this);
        mrPresidentKey_r1.setPos(0.0F, 4.95F, 9.85F);
        mrPresidentKey.addChild(mrPresidentKey_r1);
        ClientUtil.setRotationAngle(mrPresidentKey_r1, 0.0F, 0.7854F, 0.0F);
        mrPresidentKey_r1.texOffs(64, 5).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 1.0F, 3.0F, -0.15F, false);
        mrPresidentKey_r1.texOffs(76, 5).addBox(-1.5F, -0.8F, -1.5F, 3.0F, 1.0F, 3.0F, -0.25F, false);

        ModelRenderer mrPresidentKey_r2 = new ModelRenderer(this);
        mrPresidentKey_r2.setPos(0.0F, 5.4F, 9.85F);
        mrPresidentKey.addChild(mrPresidentKey_r2);
        ClientUtil.setRotationAngle(mrPresidentKey_r2, 0.0F, -0.7854F, 0.0F);
        mrPresidentKey_r2.texOffs(80, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, 0.1F, false);
        mrPresidentKey_r2.texOffs(64, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);
    }
    
    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        mrPresidentKey.xRot = body.xRot - ((float)Math.PI / 2F);
        mrPresidentKey.yRot = body.yRot;
        mrPresidentKey.zRot = body.zRot;
    }
    
    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(mrPresidentKey));
    }

    @Override
    public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, 
            int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        super.renderToBuffer(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

}
