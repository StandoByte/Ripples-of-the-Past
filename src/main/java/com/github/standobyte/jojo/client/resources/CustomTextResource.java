package com.github.standobyte.jojo.client.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomTextResource extends ReloadListener<String> {
    private String text;
    private final ResourceLocation location;

    public CustomTextResource(ResourceLocation location) {
        this.location = location;
    }

    @Override
    protected String prepare(IResourceManager resourceManager, IProfiler profiler) {
        try (
                IResource resource = Minecraft.getInstance().getResourceManager().getResource(location);
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                ) {
            return reader.lines().reduce("", (l1, l2) -> l1 + l2 + "\n");
        } catch (IOException ioexception) {
            return "";
        }
    }

    @Override
    protected void apply(String text, IResourceManager resourceManager, IProfiler profiler) {
        this.text = text;
    }
    
    @Nullable
    public String getText() {
        return text;
    }
}
