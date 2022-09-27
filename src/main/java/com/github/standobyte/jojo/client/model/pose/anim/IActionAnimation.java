package com.github.standobyte.jojo.client.model.pose.anim;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public interface IActionAnimation<T extends Entity> {
    
    void animate(StandEntityAction.Phase phase, float phaseCompletion, 
            T entity, float ticks, float yRotationOffset, float xRotation, HandSide side, boolean layer);
    
    default void renderAdditional(T entity, MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {}
}
