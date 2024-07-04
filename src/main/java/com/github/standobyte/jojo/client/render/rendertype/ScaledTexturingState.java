package com.github.standobyte.jojo.client.render.rendertype;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderState;

public class ScaledTexturingState extends RenderState.TexturingState {
    private final float xScale;
    private final float yScale;

    @SuppressWarnings("deprecation")
    public ScaledTexturingState(float xScale, float yScale) {
        super("jojo_scaled_texturing", () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.scalef(xScale, yScale, 1);
            RenderSystem.matrixMode(5888);
        }, () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        });
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ScaledTexturingState scaledState = (ScaledTexturingState)obj;
            return this.xScale == scaledState.xScale && this.yScale == scaledState.yScale;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Float.hashCode(this.xScale) + Float.hashCode(this.yScale);
    }
}
