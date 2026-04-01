package com.github.standobyte.jojo.mechanics.speechbubble;

import com.github.standobyte.jojo.mechanics.speechbubble.clowning.WorldTypingPlayers;

import net.minecraft.entity.Entity;

public class CharacterRecentlySaidSpeechBubbles {
    public SpeechBubble[] speechBubbles = new SpeechBubble[4];
    
    public void add(SpeechBubble speechBubble) {
        for (int i = 1; i < speechBubbles.length; i++) {
            if (speechBubbles[i - 1] != null && speechBubbles[i] == null) {
                speechBubbles[i] = speechBubble;
                return;
            }
        }
        
        for (int i = 0; i < speechBubbles.length; i++) {
            speechBubbles[i] = null;
        }
        speechBubbles[0] = speechBubble;
    }
    
    public void tick(Entity entity) {
        for (int i = 0; i < speechBubbles.length; i++) {
            SpeechBubble speechBubble = speechBubbles[i];
            if (speechBubble != null) {
                // don't remove it if the player is yapping, but if it's already fading away - proceed with tick (or else it flickers)
                if (!WorldTypingPlayers.isTyping(entity) || speechBubble.fadeOut.getValue(1) < 1) {
                    speechBubble.fadeOut.tick(); // 1.21.1 calls this automatically, which means there will have to be another workaround
                }
                if (!speechBubble.fadeOut.shouldRender()) {
                    speechBubbles[i] = null;
                }
            }
        }
    }
    
}
