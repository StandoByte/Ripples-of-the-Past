package com.github.standobyte.jojo.client.resources;

import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.StandGlowLayer;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

public class StandGlowTextureChecker extends ReloadListener<Void> {

    @Override
    protected Void prepare(IResourceManager resourceManager, IProfiler profiler) {
        return null;
    }

    @Override
    protected void apply(Void __, IResourceManager resourceManager, IProfiler profiler) {
        for (StandGlowLayer<?, ?> layer : StandGlowLayer.allGlowLayers()) {
            layer.setNeedsTexRecheck();
        }
    }
}
