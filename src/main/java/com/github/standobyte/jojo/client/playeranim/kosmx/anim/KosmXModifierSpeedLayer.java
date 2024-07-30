package com.github.standobyte.jojo.client.playeranim.kosmx.anim;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;

public class KosmXModifierSpeedLayer<T extends IAnimation> extends ModifierLayer<T> {
    public final SpeedModifier speed;
    
    public KosmXModifierSpeedLayer(SpeedModifier speed) {
        this.speed = speed;
        addModifierLast(speed);
    }

}
