package com.github.standobyte.jojo.client.render.world.shader;

import java.io.IOException;

import com.github.standobyte.jojo.client.ClientEventHandler;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.resources.IResourceManager;

public class TimeStopShader extends Shader {
    private final float effectLength;

    public TimeStopShader(IResourceManager resourceManager, String name, 
            Framebuffer inTarget, Framebuffer outTarget, float effectLength) throws IOException {
        super(resourceManager, name, inTarget, outTarget);
        this.effectLength = effectLength;
    }

    @Override
    public void process(float partialSecond) {
        ClientEventHandler.getInstance().addTsShaderUniforms(getEffect(), partialSecond, effectLength);
        super.process(partialSecond);
    }
}
