package com.github.standobyte.jojo.mechanics.speechbubble;

import com.github.standobyte.jojo.client.ui.actionshud.ElementTransparency;

import net.minecraft.util.text.ITextComponent;

public class SpeechBubble {
    public SpeechBubbleType type;
    public ITextComponent text;
    public boolean menacing;
    public ElementTransparency fadeOut;
    
    public SpeechBubble(SpeechBubbleType type, ITextComponent text, boolean menacing, ElementTransparency fadeOut) {
        this.type = type;
        this.text = text;
        this.menacing = menacing;
        this.fadeOut = fadeOut;
    }
    
}
