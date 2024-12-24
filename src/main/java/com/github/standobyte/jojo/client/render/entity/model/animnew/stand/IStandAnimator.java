package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public interface IStandAnimator {
    <T extends StandEntity> boolean poseStand(@Nullable T entity, StandEntityModel<T> model, StandPoseData pose, 
            float ticks, float yRotOffsetDeg, float xRotDeg);
    
    <T extends StandEntity> void addBarrageSwings(T entity, StandEntityModel<T> model, float ticks);
    <T extends StandEntity> void renderBarrageSwings(T entity, StandEntityModel<T> model, float yRotOffsetDeg, float xRotDeg, 
            MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
}
