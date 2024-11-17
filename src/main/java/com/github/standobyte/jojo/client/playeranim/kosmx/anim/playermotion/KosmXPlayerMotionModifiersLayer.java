package com.github.standobyte.jojo.client.playeranim.kosmx.anim.playermotion;

import org.jetbrains.annotations.Nullable;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;

@Deprecated
public class KosmXPlayerMotionModifiersLayer<T extends IAnimation> extends ModifierLayer<T> {
    private KosmXFrontMotionModifier playerMotionModifier;
    
    public KosmXPlayerMotionModifiersLayer(@Nullable T animation, AbstractModifier... modifiers) {
        super(animation, modifiers);
    }

    public KosmXPlayerMotionModifiersLayer() {
        super();
    }
    
    
    public void setPlayerMotionModifier(KosmXFrontMotionModifier modifier) {
        if (modifier != null) {
            addModifierLast(modifier);
        }
        this.playerMotionModifier = modifier;
    }
    
    public KosmXFrontMotionModifier getPlayerMotionModifier() {
        return playerMotionModifier;
    }
    
}
