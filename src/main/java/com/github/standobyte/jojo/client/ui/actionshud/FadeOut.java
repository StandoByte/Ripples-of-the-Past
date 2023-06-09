package com.github.standobyte.jojo.client.ui.actionshud;

public class FadeOut {
    protected final int ticksMax;
    protected final int ticksStartFadeOut;
    protected int ticks;
    
    FadeOut(int ticksMax, int ticksStartFadeOut) {
        this.ticksMax = ticksMax;
        this.ticksStartFadeOut = ticksStartFadeOut;
        this.ticks = 0;
    }
    
    void reset() {
        ticks = ticksMax;
    }
    
    void tick() {
        if (ticks > 0) {
            ticks--;
        }
    }
    
    float getValue(float partialTick) {
        if (ticks >= ticksStartFadeOut) {
            return 1F;
        }
        if (ticks <= 0) {
            return 0F;
        }
        return (ticks - partialTick) / (float) ticksStartFadeOut;
    }
}
