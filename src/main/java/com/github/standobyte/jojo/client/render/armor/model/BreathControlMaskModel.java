package com.github.standobyte.jojo.client.render.armor.model;

import java.util.Collections;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

// Made with Blockbench 3.9.2


public class BreathControlMaskModel extends BipedModel<LivingEntity> {

    public BreathControlMaskModel(float size) {
        super(size);
        texWidth = 32;
        texHeight = 32;

        head.setTexSize(texWidth, texHeight);
        ClientUtil.clearCubes(head);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 3.0F, 3.0F, 0.4F, false);
        head.texOffs(22, 0).addBox(-1.0F, -2.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.6F, false);
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