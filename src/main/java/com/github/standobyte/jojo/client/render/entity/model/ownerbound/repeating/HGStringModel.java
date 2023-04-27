package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class HGStringModel<T extends OwnerBoundProjectileEntity> extends RepeatingModel<T> {
    private final ModelRenderer barrier;

    public HGStringModel() {
        texWidth = 32;
        texHeight = 32;

        barrier = new ModelRenderer(this);
        barrier.setPos(0.0F, 0.0F, 0.0F);
        barrier.texOffs(0, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 8.0F, 0.0F, false);
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
        return barrier;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 8F;
    }
}
