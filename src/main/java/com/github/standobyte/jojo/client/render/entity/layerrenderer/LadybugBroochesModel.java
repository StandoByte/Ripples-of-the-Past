package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class LadybugBroochesModel<T extends LivingEntity> extends BipedModel<T> {
    public final ModelRenderer broochRight;
    public final ModelRenderer broochLeft;
    public final ModelRenderer broochBottom;

    public LadybugBroochesModel() {
        super(0);
        
        texWidth = 64;
        texHeight = 64;

        body.cubes.clear();
        
        broochRight = new ModelRenderer(this);
        broochRight.setPos(-2.85F, 4.625F, -2.4F);
        body.addChild(broochRight);
        broochRight.texOffs(24, 2).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.6F, false);

        ModelRenderer broochPin1 = new ModelRenderer(this);
        broochPin1.setPos(0.0F, -0.375F, 0.4F);
        broochRight.addChild(broochPin1);
        broochPin1.xRot = 0.3927F;
        broochPin1.texOffs(34, 5).addBox(-0.5F, -0.35F, -0.65F, 1.0F, 2.0F, 1.0F, -0.35F, false);

        broochLeft = new ModelRenderer(this);
        broochLeft.setPos(2.85F, 4.625F, -2.4F);
        body.addChild(broochLeft);
        broochLeft.texOffs(24, 2).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.6F, false);

        ModelRenderer broochPin2 = new ModelRenderer(this);
        broochPin2.setPos(0.0F, -0.375F, 0.4F);
        broochLeft.addChild(broochPin2);
        broochPin2.xRot = 0.3927F;
        broochPin2.texOffs(34, 5).addBox(-0.5F, -0.35F, -0.65F, 1.0F, 2.0F, 1.0F, -0.35F, false);

        broochBottom = new ModelRenderer(this);
        broochBottom.setPos(0.05F, 10.125F, -2.4F);
        body.addChild(broochBottom);
        broochBottom.texOffs(24, 2).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.6F, false);

        ModelRenderer broochPin3 = new ModelRenderer(this);
        broochPin3.setPos(0.0F, -0.375F, 0.4F);
        broochBottom.addChild(broochPin3);
        broochPin3.xRot = 0.3927F;
        broochPin3.texOffs(34, 5).addBox(-0.5F, -0.35F, -0.65F, 1.0F, 2.0F, 1.0F, -0.35F, false);
    }
    
    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(body);
    }

}
