package com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SpaceRipperStingyEyesEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class SpaceRipperStingyEyesRenderer extends BeamRenderer<SpaceRipperStingyEyesEntity> {
    private static final ResourceLocation BEAM_TEX = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/space_ripper_stingy_eyes.png");

    public SpaceRipperStingyEyesRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }
    
    @Override
    public ResourceLocation getTextureLocation(SpaceRipperStingyEyesEntity entity) {
        return BEAM_TEX;
    }
    
    @Override
    protected Vector3d pointB(SpaceRipperStingyEyesEntity entity, float partialTick) {
        return entity.getOriginPoint(partialTick);
    }

    @Override
    protected float getBeamWidth(SpaceRipperStingyEyesEntity entity) {
        return 0.05F;
    }
}
