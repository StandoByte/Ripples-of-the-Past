package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.client.render.entity.pose.anim.IActionAnimation;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public interface IBarrageAnimation<T extends Entity, M extends EntityModel<T>> extends IActionAnimation<T> {
    void addSwings(T entity, HandSide side, float ticks);
    void animateSwing(T entity, M model, float loopCompletion, HandSide side, float yRotOffsetRad, float xRotRad, float zRotOffsetRad);
    default void beforeSwingAfterimageRender(MatrixStack matrixStack, M model, float loopCompletion, HandSide side) {}
}
