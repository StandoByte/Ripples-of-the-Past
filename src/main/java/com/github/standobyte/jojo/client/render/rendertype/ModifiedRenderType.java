package com.github.standobyte.jojo.client.render.rendertype;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.client.renderer.RenderType;

public abstract class ModifiedRenderType extends RenderType {

    public ModifiedRenderType(RenderType original, Runnable setupState, Runnable clearState, String name) {
        super(name + "_" + original.toString() + "_" + JojoMod.MOD_ID, original.format(), original.mode(), 
                original.bufferSize(), original.affectsCrumbling(), true, 
                () -> {
                    original.setupRenderState();
                    setupState.run();
                }, 
                () -> {
                    clearState.run();
                    original.clearRenderState();
                });
    }
    
    @Override
    public boolean equals(@Nullable Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
