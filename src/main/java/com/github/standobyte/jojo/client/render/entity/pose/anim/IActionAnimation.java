package com.github.standobyte.jojo.client.render.entity.pose.anim;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public interface IActionAnimation<T extends Entity> {
    
    @Deprecated
    default void onAnimStart(T entity, float yRotationOffset, float xRotation) {}
    
    void animate(StandEntityAction.Phase phase, float phaseCompletion, 
            T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide side);
    
    default void renderAdditional(T entity, MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {}
}
