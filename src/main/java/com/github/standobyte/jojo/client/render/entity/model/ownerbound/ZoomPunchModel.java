package com.github.standobyte.jojo.client.render.entity.model.ownerbound;

import java.util.List;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ZoomPunchModel extends EntityModel<ZoomPunchEntity> {
    private final ModelRenderer mobPunchRight;
    private final ModelRenderer mobPunchLeft;
    
    private final ModelRenderer playerPunchRight;
    private final ModelRenderer playerPunchLeft;
    private final ModelRenderer playerPunchRightSlim;
    private final ModelRenderer playerPunchLeftSlim;
    
    private final ModelRenderer playerSleeveRight;
    private final ModelRenderer playerSleeveLeft;
    private final ModelRenderer playerSleeveRightSlim;
    private final ModelRenderer playerSleeveLeftSlim;
    
    private final List<ModelRenderer> armVariants;

    public ZoomPunchModel(float modelSize) {
        texWidth = 64;
        texHeight = 32;

        mobPunchRight = new ModelRenderer(this);
        mobPunchRight.xRot = -1.5708F;
        mobPunchRight.setPos(0.0F, -2.0F, 0.0F);
        mobPunchRight.texOffs(40, 16).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        mobPunchLeft = new ModelRenderer(this);
        mobPunchLeft.xRot = -1.5708F;
        mobPunchLeft.setPos(0.0F, -2.0F, 0.0F);
        mobPunchLeft.texOffs(40, 16).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        
        
        texHeight = 64;
        

        playerPunchRight = new ModelRenderer(this);
        playerPunchRight.xRot = -1.5708F;
        playerPunchRight.setPos(0.0F, -2.0F, 0.0F);
        playerPunchRight.texOffs(40, 16).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        playerPunchLeft = new ModelRenderer(this);
        playerPunchLeft.xRot = -1.5708F;
        playerPunchLeft.setPos(0.0F, -2.0F, 0.0F);
        playerPunchLeft.texOffs(32, 48).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        playerPunchRightSlim = new ModelRenderer(this);
        playerPunchRightSlim.xRot = -1.5708F;
        playerPunchRightSlim.setPos(0.0F, -2.0F, 0.0F);
        playerPunchRightSlim.texOffs(40, 16).addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        playerPunchLeftSlim = new ModelRenderer(this);
        playerPunchLeftSlim.xRot = -1.5708F;
        playerPunchLeftSlim.setPos(0.0F, -2.0F, 0.0F);
        playerPunchLeftSlim.texOffs(32, 48).addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
        

        playerSleeveRight = new ModelRenderer(this);
        playerSleeveRight.xRot = -1.5708F;
        playerSleeveRight.setPos(0.0F, -2.0F, 0.0F);
        playerSleeveRight.texOffs(40, 32).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        playerSleeveLeft = new ModelRenderer(this);
        playerSleeveLeft.xRot = -1.5708F;
        playerSleeveLeft.setPos(0.0F, -2.0F, 0.0F);
        playerSleeveLeft.texOffs(48, 48).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        playerSleeveRightSlim = new ModelRenderer(this);
        playerSleeveRightSlim.xRot = -1.5708F;
        playerSleeveRightSlim.setPos(0.0F, -2.0F, 0.0F);
        playerSleeveRightSlim.texOffs(40, 32).addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);

        playerSleeveLeftSlim = new ModelRenderer(this);
        playerSleeveLeftSlim.xRot = -1.5708F;
        playerSleeveLeftSlim.setPos(0.0F, -2.0F, 0.0F);
        playerSleeveLeftSlim.texOffs(48, 48).addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);
        
        
        armVariants = ImmutableList.of(
                mobPunchRight, 
                mobPunchLeft, 
                playerPunchRight, 
                playerPunchLeft, 
                playerPunchRightSlim, 
                playerPunchLeftSlim, 
                playerSleeveRight, 
                playerSleeveLeft, 
                playerSleeveRightSlim, 
                playerSleeveLeftSlim);
    }

    @Override
    public void setupAnim(ZoomPunchEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        float yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
        float xRot = -1.5708F + xRotation * MathUtil.DEG_TO_RAD;
        for (ModelRenderer arm : armVariants) {
            if (arm.visible) {
                arm.yRot = yRot;
                arm.xRot = xRot;
            }
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        for (ModelRenderer arm : armVariants) {
            arm.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
    
    public void setVisibility(boolean leftSide, boolean player, boolean playerSlim) {
        for (ModelRenderer arm : armVariants) {
            arm.visible = false;
        }
        ModelRenderer visibleArm;
        if (player) {
            ModelRenderer visibleSleeve;
            if (playerSlim) {
                visibleArm = leftSide ? playerPunchLeftSlim : playerPunchRightSlim;
                visibleSleeve = leftSide ? playerSleeveLeftSlim : playerSleeveRightSlim;
            }
            else {
                visibleArm = leftSide ? playerPunchLeft : playerPunchRight;
                visibleSleeve = leftSide ? playerSleeveLeft : playerSleeveRight;
            }
            visibleSleeve.visible = true;
        }
        else {
            visibleArm = leftSide ? mobPunchLeft : mobPunchRight;
        }
        visibleArm.visible = true;
    }
}
