package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.render.item.CustomIconMapRender;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.world.storage.MapData;

@Mixin(MapItemRenderer.class)
public class MapItemRendererMixin {

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void jojoOnRenderMapIcon(MatrixStack matrixStack, IRenderTypeBuffer buffer, 
            MapData mapData, boolean active, int packedLight, CallbackInfo ci) {
        CustomIconMapRender.clCaptureIconRenderArgs(matrixStack, buffer, active, packedLight);
    }
}
