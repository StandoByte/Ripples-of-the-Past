package com.github.standobyte.jojo.mechanics.speechbubble.client;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mechanics.speechbubble.SpeechBubbleType;
import com.github.standobyte.jojo.mechanics.speechbubble.clowning.WorldTypingPlayers;

import net.minecraft.client.Minecraft;

public enum ClientChatMode {
    VANILLA,
    SPEECH,
    SPEECH_MENACING,
    SHOUT,
    THOUGHT;
    
    @Nullable
    public SpeechBubbleType getSpeechBubble() {
        switch (this) {
            case SPEECH:
            case SPEECH_MENACING:
                return SpeechBubbleType.REGULAR;
            case SHOUT:
                return SpeechBubbleType.SHOUT;
            case THOUGHT:
                return SpeechBubbleType.THOUGHT;
            default:
                return null;
        }
    }
    
    public boolean gogogogogoParticles() {
        return this == ClientChatMode.SPEECH_MENACING;
    }
    


    public static ClientChatMode curChatMode = ClientChatMode.VANILLA;
    
    public static boolean pause(ClientChatMode mode, boolean isTypingCommand) {
        return mode != ClientChatMode.VANILLA && !isTypingCommand;
    }
    
    public static void setChatMode(ClientChatMode chatMode) {
        boolean prevState = pause(curChatMode, ChatScreenSpeechBubbleHandler.isTypingCommand);
        boolean newState = pause(chatMode, ChatScreenSpeechBubbleHandler.isTypingCommand);
        curChatMode = chatMode;
        if (prevState != newState) {
            WorldTypingPlayers.clientSideChangedState(Minecraft.getInstance().player, newState);
        }
    }
    
    public static void onChangedTypingCommand(boolean isTypingCommand) {
        boolean typingState = pause(curChatMode, isTypingCommand);
        WorldTypingPlayers.clientSideChangedState(Minecraft.getInstance().player, typingState);
    }
    
    public static void onOpenedChat() {
        boolean typingState = pause(curChatMode, ChatScreenSpeechBubbleHandler.isTypingCommand);
        WorldTypingPlayers.clientSideChangedState(Minecraft.getInstance().player, typingState);
    }
    
    public static void onClosedChat() {
        WorldTypingPlayers.clientSideChangedState(Minecraft.getInstance().player, false);
    }
    
}
