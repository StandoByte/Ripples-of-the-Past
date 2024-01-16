package com.github.standobyte.jojo.client.render.world.shader;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomShaderGroup extends ShaderGroup {

    public CustomShaderGroup(TextureManager textureManager, IResourceManager resourceManager, 
            Framebuffer screenTarget, ResourceLocation name) throws IOException, JsonSyntaxException {
        super(textureManager, resourceManager, screenTarget, name);
    }

    @Override
    public Shader addPass(String name, Framebuffer inTarget, Framebuffer outTarget) throws IOException {
        Shader shader = getCustomParametersShader(Minecraft.getInstance().getResourceManager(), name, inTarget, outTarget);
        if (shader == null) {
            return super.addPass(name, inTarget, outTarget);
        }
        List<Shader> passes = ClientReflection.getShaderGroupPasses(this);
        passes.add(passes.size(), shader);
        return shader;
    }
    
    @Nullable
    protected Shader getCustomParametersShader(IResourceManager resourceManager, String name, 
            Framebuffer inTarget, Framebuffer outTarget) throws IOException {
        if ("jojo:time_stop".equals(name)) {
            return new TimeStopShader(resourceManager, name, inTarget, outTarget, 35F);
        }
        return null;
    }
}
