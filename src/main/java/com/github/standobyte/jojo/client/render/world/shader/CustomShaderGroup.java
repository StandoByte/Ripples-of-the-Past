package com.github.standobyte.jojo.client.render.world.shader;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
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
    
    public static final ResourceLocation TIME_STOP_PREV_EFFECT = new ResourceLocation("shaders/post/desaturate.json");
    
    public static final ResourceLocation TIME_STOP_TW = new ResourceLocation(JojoMod.MOD_ID, "shaders/post/time_stop_tw.json");
    public static final ResourceLocation TIME_STOP_SP = new ResourceLocation(JojoMod.MOD_ID, "shaders/post/time_stop_sp.json");
    
    public static boolean hasCustomParameters(ResourceLocation shaderGroup) {
        return TIME_STOP_TW.equals(shaderGroup) || TIME_STOP_SP.equals(shaderGroup);
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
