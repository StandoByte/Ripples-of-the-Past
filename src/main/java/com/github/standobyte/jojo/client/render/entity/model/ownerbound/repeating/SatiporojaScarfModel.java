package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class SatiporojaScarfModel extends RepeatingModel<SatiporojaScarfEntity> {
    private final ModelRenderer scarfExtending;

    public SatiporojaScarfModel() {
        texWidth = 32;
        texHeight = 32;

        scarfExtending = new ModelRenderer(this);
        scarfExtending.setPos(0.0F, 0.0F, 0.0F);
        scarfExtending.texOffs(0, 10).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 12.0F, -0.3F, false);
    }

    @Override
    protected ModelRenderer getMainPart() {
        return null;
    }
    
    @Override
    protected float getMainPartLength() {
        return 0;
    }
    
    @Override
    protected ModelRenderer getRepeatingPart() {
        return scarfExtending;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 11.4F;
    }
}
