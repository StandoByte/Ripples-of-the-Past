package com.github.standobyte.jojo.client.renderer.entity.damaging;

import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MRFlameRenderer extends EntityRenderer<MRFlameEntity> {

    public MRFlameRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(MRFlameEntity p_110775_1_) {
        return null;
    }

    private static final double STEP_LENGTH = 0.75D;
    @Override
    public void render(MRFlameEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
            Vector3d pos = entity.getPosition(partialTick);
            Vector3d vec = entity.getStartingPos().subtract(pos);
            double length = vec.length();
            Vector3d step = vec.scale(STEP_LENGTH / length);
            for (int i = MathHelper.floor(length / STEP_LENGTH); i > 0; i--) {
                entity.level.addAlwaysVisibleParticle(ModParticles.FLAME_ONE_TICK.get(), true, pos.x, pos.y, pos.z, 0, 0, 0);
                pos = pos.add(step);
            }
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
}
