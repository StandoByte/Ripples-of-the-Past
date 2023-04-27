package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import com.github.standobyte.jojo.entity.damaging.projectile.SCFlameSwingEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.math.vector.Vector3d;

public class SCFlameRenderer extends FlameRenderer<SCFlameSwingEntity> {

    public SCFlameRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }
    
    @Override
    protected Vector3d getStartingPos(SCFlameSwingEntity entity) {
        return entity.getStartingPos();
    }
}
