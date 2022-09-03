package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public abstract class AdditionalBarrageSwing<T extends StandEntity> {
    protected final IBarrageAnimation<T> barrageAnim;
    protected float ticks;
    protected final float ticksMax;
    
    public AdditionalBarrageSwing(IBarrageAnimation<T> barrageAnim, float startingAnim, float animMax) {
        this.barrageAnim = barrageAnim;
        this.ticks = startingAnim;
        this.ticksMax = animMax;
    }
    
    public void addDelta(float delta) {
        ticks += delta * 0.75F;
    }
    
    public boolean removeSwing() {
        return ticks >= ticksMax * 0.75F;
    }
    
    
    
    public abstract void poseAndRender(T entity, StandEntityModel<T> model, MatrixStack matrixStack, IVertexBuilder buffer, 
            float yRotationOffset, float xRotation, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
}
