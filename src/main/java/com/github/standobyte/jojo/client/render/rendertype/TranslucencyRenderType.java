package com.github.standobyte.jojo.client.render.rendertype;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderType;

public class TranslucencyRenderType extends ModifiedRenderType {

    public TranslucencyRenderType(RenderType original) {
        super(original, 
                () -> {
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
                    RenderSystem.blendColor(1, 1, 1, 0.3F);
                }, 
                () -> {
                    RenderSystem.blendColor(1, 1, 1, 1);
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                },
                "translucent");
    }

}
