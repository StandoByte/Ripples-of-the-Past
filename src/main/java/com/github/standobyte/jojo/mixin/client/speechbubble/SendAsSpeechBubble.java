package com.github.standobyte.jojo.mixin.client.speechbubble;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.mechanics.speechbubble.SpeechBubblesFunctionality;
import com.github.standobyte.jojo.mechanics.speechbubble.client.ClientChatMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;

// 1.21.1 - mixing to ChatScreen
@Mixin(Screen.class)
public class SendAsSpeechBubble {
    @Shadow protected Minecraft minecraft;

    // 1.21.1 - inject to handleChatInput
    @Inject(method = "sendMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    public void sendAsDialogueInstead(String message, boolean addToRecentChat, CallbackInfo ci) {
        ClientChatMode curChatMode = ClientChatMode.curChatMode;
        Entity entity = minecraft.getCameraEntity();
        if (entity == null) entity = minecraft.player;
        if (SpeechBubblesFunctionality.clientSideSendChatMessage(curChatMode, message, entity)) {
            ci.cancel();
        }
    }
}
