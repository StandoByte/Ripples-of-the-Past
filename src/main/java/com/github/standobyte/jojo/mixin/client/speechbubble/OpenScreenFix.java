package com.github.standobyte.jojo.mixin.client.speechbubble;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Minecraft.class)
public abstract class OpenScreenFix {

    @Shadow public abstract void setScreen(@Nullable Screen guiScreen);

    @Inject(method = "openChatScreen", at = @At("HEAD"), cancellable = true)
    public void fixOpenChat(String defaultText, CallbackInfo ci) {
        this.setScreen(new ChatScreen(defaultText));
        ci.cancel();
    }
}
