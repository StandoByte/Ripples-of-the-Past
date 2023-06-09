package com.github.standobyte.jojo.client.ui.actionshud;

public class ElementTransparency extends FadeOut {
    
    ElementTransparency(int ticksMax, int ticksStartFadeOut) {
        super(ticksMax, ticksStartFadeOut);
    }
    
    boolean shouldRender() {
        return ticks > 0;
    }
    
    int makeTextColorTranclucent(int color, float partialTick) {
        return addAlpha(color, getAlpha(partialTick));
    }
    
    private static final float MIN_ALPHA = 1F / 63F;
    float getAlpha(float partialTick) {
        return ticks > 0 ? Math.max(getValue(partialTick), MIN_ALPHA) : 0;
    }
    
    static int addAlpha(int color, float alpha) {
        return color | ((int) (255F * alpha)) << 24 & -0x1000000;
    }
}
