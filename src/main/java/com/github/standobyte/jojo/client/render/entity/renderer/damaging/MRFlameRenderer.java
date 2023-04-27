package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.math.vector.Vector3d;

public class MRFlameRenderer extends FlameRenderer<MRFlameEntity> {

    public MRFlameRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }
    
    @Override
    protected Vector3d getStartingPos(MRFlameEntity entity) {
        return entity.getStartingPos();
    }
}
