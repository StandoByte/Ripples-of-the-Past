package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class SPStarFingerModel extends RepeatingModel<SPStarFingerEntity> {
    private final ModelRenderer finger;
    private final ModelRenderer fingerExtending;

    public SPStarFingerModel() {
        texWidth = 32;
        texHeight = 32;

        finger = new ModelRenderer(this);
        finger.setPos(0.0F, 0.0F, 0.0F);
        finger.texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        fingerExtending = new ModelRenderer(this);
        fingerExtending.setPos(0.0F, 0.0F, 0.0F);
        fingerExtending.texOffs(0, 0).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 1.0F, 8.0F, 0.0F, false);
    }

    @Override
    protected ModelRenderer getMainPart() {
        return finger;
    }
    
    @Override
    protected float getMainPartLength() {
        return 2F;
    }
    
    @Override
    protected ModelRenderer getRepeatingPart() {
        return fingerExtending;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 8F;
    }
}
