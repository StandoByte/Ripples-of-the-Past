package com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.damaging.LightBeamEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class LightBeamRenderer<T extends LightBeamEntity> extends BeamRenderer<T> {
    private static final ResourceLocation BEAM_TEX = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/aja_beam.png");

    public LightBeamRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return BEAM_TEX;
    }
    
    @Override
    protected Vector3d pointB(T entity, float partialTick) {
        return entity.getEndPoint();
    }

    @Override
    protected float getBeamWidth(T entity) {
        return entity.getBaseDamage() * 0.002F;
    }
}
