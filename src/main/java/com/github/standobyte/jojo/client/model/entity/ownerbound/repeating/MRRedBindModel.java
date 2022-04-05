package com.github.standobyte.jojo.client.model.entity.ownerbound.repeating;

import com.github.standobyte.jojo.client.model.FlameModelRenderer;
import com.github.standobyte.jojo.client.renderer.entity.stand.MagiciansRedRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;

// Made with Blockbench 3.9.2


public class MRRedBindModel extends RepeatingModel<MRRedBindEntity> {
    private final FlameModelRenderer flameRope;

    public MRRedBindModel() {
        texWidth = 32;
        texHeight = 32;

        flameRope = new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0, MagiciansRedRenderer.FIRE_1);
        flameRope.setPos(0.0F, 0.0F, 0.0F);
        flameRope.addFlame(0, 1.0F, 0, 2F, 3F, Direction.NORTH);
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
        return flameRope;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 3F;
    }
}
