package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.SpectatorGui;

@Mixin(SpectatorGui.class)
public class SpectatorGuiMixin {

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    public void jojoCancelTooltipRender(MatrixStack matrixStack, CallbackInfo ci) {
        if (cancelRender()) ci.cancel();
    }

    @Inject(method = "onHotbarSelected", at = @At("HEAD"), cancellable = true)
    public void jojoCancelHotbarSlotSelect(int slot, CallbackInfo ci) {
        if (cancelRender()) ci.cancel();
    }

    @Inject(method = "onMouseScrolled", at = @At("HEAD"), cancellable = true)
    public void jojoCancelHotbarScroll(double amount, CallbackInfo ci) {
        if (cancelRender()) ci.cancel();
    }

    @Inject(method = "onMouseMiddleClick", at = @At("HEAD"), cancellable = true)
    public void jojoCancelMMB(CallbackInfo ci) {
        if (cancelRender()) ci.cancel();
    }
    
    private static boolean cancelRender() {
        return IPlayerPossess.getPossessedEntity(Minecraft.getInstance().player) != null;
    }
}
