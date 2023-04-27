package com.github.standobyte.jojo.util.mc;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class OstSoundList {
    private static final String[] POSTFIXES = {"_30", "_60", "_75", "_90", "_120", "_full"};
    private final List<RegistryObject<SoundEvent>> soundEvents;
    
    public OstSoundList(ResourceLocation resLoc, DeferredRegister<SoundEvent> register) {
        ImmutableList.Builder<RegistryObject<SoundEvent>> listBuilder = ImmutableList.builder();
        for (String postfix : POSTFIXES) {
            String path = resLoc.getPath() + postfix;
            listBuilder.add(register.register(path, () -> new SoundEvent(new ResourceLocation(resLoc.getNamespace(), path))));
        }
        soundEvents = listBuilder.build();
    }
    
    public SoundEvent get(int index) {
        return soundEvents.get(MathHelper.clamp(index, 0, soundEvents.size() - 1)).get();
    }

}
