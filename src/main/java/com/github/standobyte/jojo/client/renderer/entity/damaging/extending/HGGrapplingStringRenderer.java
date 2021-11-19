package com.github.standobyte.jojo.client.renderer.entity.damaging.extending;

import com.github.standobyte.jojo.client.model.entity.ownerbound.repeating.HGStringModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGGrapplingStringEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class HGGrapplingStringRenderer extends HGStringAbstractRenderer<HGGrapplingStringEntity> {

    public HGGrapplingStringRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HGStringModel<HGGrapplingStringEntity>());
    }
}
