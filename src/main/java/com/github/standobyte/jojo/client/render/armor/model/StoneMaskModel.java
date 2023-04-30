package com.github.standobyte.jojo.client.render.armor.model;

import java.util.Collections;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

// Made with Blockbench 3.9.2


public class StoneMaskModel extends BipedModel<LivingEntity> {

    public StoneMaskModel(float size) {
        super(size);
        texWidth = 32;
        texHeight = 32;

        head.setTexSize(texWidth, texHeight);
        ClientUtil.clearCubes(head);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(2, 2).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 6.0F, 0.55F, false);
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