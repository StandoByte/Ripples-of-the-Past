package com.github.standobyte.jojo.client.render.armor.model;

import java.util.Collections;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

// Made with Blockbench 3.9.2


public class BladeHatArmorModel extends BipedModel<LivingEntity> {
    private final ModelRenderer pink_left;
    private final ModelRenderer pink_right;

    public BladeHatArmorModel(float size) {
        super(size);
        texWidth = 32;
        texHeight = 32;

        head.setTexSize(texWidth, texHeight);
        ClientUtil.clearCubes(head);
        head.setPos(0.0F, 0.5F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.5F, -4.0F, 8.0F, 3.0F, 8.0F, 0.75F, false);
        head.texOffs(0, 25).addBox(-3.0F, -10.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.75F, false);
        head.texOffs(0, 12).addBox(-6.0F, -4.75F, -6.0F, 12.0F, 0.0F, 12.0F, 0.0F, false);

        pink_left = new ModelRenderer(this);
        pink_left.setPos(-5.25F, -7.5F, 0.0F);
        head.addChild(pink_left);
        setRotationAngle(pink_left, 0.0F, 0.0F, -0.3491F);
        pink_left.texOffs(0, 25).addBox(0.0F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, false);

        pink_right = new ModelRenderer(this);
        pink_right.setPos(5.25F, -7.5F, 0.0F);
        head.addChild(pink_right);
        setRotationAngle(pink_right, 0.0F, 0.0F, 0.3491F);
        pink_right.texOffs(0, 25).addBox(0.0F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, false);
    }
    
    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return Collections.emptyList();
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}