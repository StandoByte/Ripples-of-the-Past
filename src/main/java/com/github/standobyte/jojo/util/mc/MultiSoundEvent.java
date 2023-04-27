package com.github.standobyte.jojo.util.mc;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

// for voice lines with the same usage but different subtitles
public class MultiSoundEvent extends SoundEvent {
    private static final Random RANDOM = new Random();
    private final ResourceLocation[] resLocs;

    public MultiSoundEvent(ResourceLocation resLocFirst, ResourceLocation... resLocsNext) {
        super(resLocFirst);
        this.resLocs = new ResourceLocation[resLocsNext.length + 1];
        this.resLocs[0] = resLocFirst;
        System.arraycopy(resLocsNext, 0, this.resLocs, 1, resLocsNext.length);
    }
    
    @Override
    public ResourceLocation getLocation() {
        return resLocs[RANDOM.nextInt(resLocs.length)];
    }

}
