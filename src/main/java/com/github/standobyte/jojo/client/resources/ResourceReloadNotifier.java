package com.github.standobyte.jojo.client.resources;

import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.sound.StandCrySoundHandler;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

public class ResourceReloadNotifier extends ReloadListener<Void> {

    @Override
    protected Void prepare(IResourceManager resourceManager, IProfiler profiler) {
        return null;
    }

    @Override
    protected void apply(Void __, IResourceManager resourceManager, IProfiler profiler) {
        ResourcePathChecker.onResourcesReload();
        StandCrySoundHandler.clearCache();
    }
}
