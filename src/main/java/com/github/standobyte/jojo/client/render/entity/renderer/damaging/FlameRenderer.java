package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import com.github.standobyte.jojo.init.ModParticles;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public abstract class FlameRenderer<T extends Entity> extends EntityRenderer<T> {

    public FlameRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }

    private static final double STEP_LENGTH = 0.4D;
    @Override
    public void render(T entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
            Vector3d pos = entity.getPosition(partialTick);
            Vector3d vec = getStartingPos(entity).subtract(pos);
            double length = vec.length();
            Vector3d step = vec.scale(STEP_LENGTH / length);
            for (int i = MathHelper.floor(length / STEP_LENGTH); i > 0; i--) {
                entity.level.addAlwaysVisibleParticle(ModParticles.FLAME_ONE_TICK.get(), true, pos.x, pos.y, pos.z, 0, 0, 0);
                pos = pos.add(step);
            }
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected abstract Vector3d getStartingPos(T entity);
}
