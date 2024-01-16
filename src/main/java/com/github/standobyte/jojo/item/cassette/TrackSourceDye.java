package com.github.standobyte.jojo.item.cassette;

import java.util.Map;
import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.collect.Maps;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;

public class TrackSourceDye extends TrackSource {
    private final DyeColor color;

    public TrackSourceDye(DyeColor color) {
        super(TrackSourceType.DYE_COLOR);
        this.color = color;
    }

    protected static TrackSource fromNBT(CompoundNBT nbt) {
        if (nbt.contains("Color", MCUtil.getNbtId(IntNBT.class))) {
            int colorOrdinal = nbt.getInt("Color");
            if (colorOrdinal >= 0 && colorOrdinal < DyeColor.values().length) {
                return new TrackSourceDye(DyeColor.values()[colorOrdinal]);
            }
        }

        return BROKEN_CASSETTE;
    }

    @Override
    protected CompoundNBT toNBT() {
        CompoundNBT nbt = super.toNBT();
        nbt.putInt("Color", color.ordinal());
        return nbt;
    }

    @Override
    public SoundEvent getSoundEvent() {
        if (color == null) return null;
        return SOUND_EVENT_BY_DYE.get(color).get();
    }

    @Override
    protected String getTranslationKey(ResourceLocation trackId) {
        return trackId.getNamespace() + "." + trackId.getPath().replace("/", ".") + ".name";
    }

    
    private static final Map<DyeColor, Supplier<SoundEvent>> SOUND_EVENT_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), map -> {
        map.put(DyeColor.WHITE, ModSounds.CASSETTE_WHITE);
        map.put(DyeColor.ORANGE, ModSounds.CASSETTE_ORANGE);
        map.put(DyeColor.MAGENTA, ModSounds.CASSETTE_MAGENTA);
        map.put(DyeColor.LIGHT_BLUE, ModSounds.CASSETTE_LIGHT_BLUE);
        map.put(DyeColor.YELLOW, ModSounds.CASSETTE_YELLOW);
        map.put(DyeColor.LIME, ModSounds.CASSETTE_LIME);
        map.put(DyeColor.PINK, ModSounds.CASSETTE_PINK);
        map.put(DyeColor.GRAY, ModSounds.CASSETTE_GRAY);
        map.put(DyeColor.LIGHT_GRAY, ModSounds.CASSETTE_LIGHT_GRAY);
        map.put(DyeColor.CYAN, ModSounds.CASSETTE_CYAN);
        map.put(DyeColor.PURPLE, ModSounds.CASSETTE_PURPLE);
        map.put(DyeColor.BLUE, ModSounds.CASSETTE_BLUE);
        map.put(DyeColor.BROWN, ModSounds.CASSETTE_BROWN);
        map.put(DyeColor.GREEN, ModSounds.CASSETTE_GREEN);
        map.put(DyeColor.RED, ModSounds.CASSETTE_RED);
        map.put(DyeColor.BLACK, ModSounds.CASSETTE_BLACK);
    });
}
