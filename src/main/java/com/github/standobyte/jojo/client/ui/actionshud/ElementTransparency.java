package com.github.standobyte.jojo.client.ui.actionshud;

import com.github.standobyte.jojo.client.ClientUtil;

public class ElementTransparency extends FadeOut {
    
    public ElementTransparency(int ticksMax, int ticksStartFadeOut) {
        super(ticksMax, ticksStartFadeOut);
        reset();
    }
    
    public boolean shouldRender() {
        return ticks > 0;
    }
    
    public int makeTextColorTranclucent(int color, float partialTick) {
        return ClientUtil.addAlpha(color, getAlpha(partialTick));
    }
    
    public static final float MIN_ALPHA = 1F / 63F;
    public float getAlpha(float partialTick) {
        return ticks > 0 ? Math.max(getValue(partialTick), MIN_ALPHA) : 0;
    }
}
