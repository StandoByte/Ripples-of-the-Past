package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;

public class BarrageSwingsHolder<T extends StandEntity> {
    private List<AdditionalBarrageSwing<T>> barrageSwings = new LinkedList<>();
    private float loopLast = -1;

    public void addSwing(AdditionalBarrageSwing<T> swing) {
        barrageSwings.add(swing);
    }
    
    public void updateSwings(Minecraft mc) {
        if (!mc.isPaused() && !barrageSwings.isEmpty()) {
            float timeDelta = mc.getDeltaFrameTime();
            Iterator<AdditionalBarrageSwing<T>> iter = barrageSwings.iterator();
            while (iter.hasNext()) {
                AdditionalBarrageSwing<T> swing = iter.next();
                swing.addDelta(timeDelta);
                if (swing.removeSwing()) {
                    iter.remove();
                }
            }
        }
    }

    public void renderBarrageSwings(StandEntityModel<T> model, T entity, MatrixStack matrixStack, IVertexBuilder buffer, 
            int packedLight, int packedOverlay, float yRotation, float xRotation, float red, float green, float blue, float alpha) {
        if (!barrageSwings.isEmpty()) {
            for (AdditionalBarrageSwing<T> swing : barrageSwings) {
                swing.poseAndRender(entity, model, matrixStack, buffer, yRotation, xRotation, packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
    }
    
    public void setLoopCount(float loopCount) {
        this.loopLast = loopCount;
    }
    
    public float getLoopCount() {
        return loopLast;
    }
}
