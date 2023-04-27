package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfBindingEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class SatiporojaScarfBindingModel extends RepeatingModel<SatiporojaScarfBindingEntity> {
    private final ModelRenderer scarf;
    private final ModelRenderer scarfExtending;

    public SatiporojaScarfBindingModel() {
        texWidth = 32;
        texHeight = 32;

        scarf = new ModelRenderer(this);
        scarf.setPos(0.0F, 0.0F, 0.0F);
        scarf.texOffs(0, 0).addBox(-4.0F, -2.5F, -0.5F, 8.0F, 2.0F, 8.0F, 0.2F, false);

        scarfExtending = new ModelRenderer(this);
        scarfExtending.setPos(0.0F, 0.0F, 0.0F);
        scarfExtending.texOffs(0, 10).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 12.0F, -0.3F, false);
    }

    @Override
    protected ModelRenderer getMainPart() {
        return scarf;
    }
    
    @Override
    protected float getMainPartLength() {
        return 8.4F;
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
