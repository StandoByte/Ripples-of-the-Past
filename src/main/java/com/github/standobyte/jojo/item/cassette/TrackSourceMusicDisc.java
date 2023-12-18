package com.github.standobyte.jojo.item.cassette;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class TrackSourceMusicDisc extends TrackSource {
    private final MusicDiscItem musicDisc;
    
    public TrackSourceMusicDisc(MusicDiscItem musicDisc) {
        super(TrackSourceType.MUSIC_DISC);
        this.musicDisc = musicDisc;
    }
    
    static TrackSource fromNBT(CompoundNBT nbt) {
        if (nbt.contains("MusicDisc", MCUtil.getNbtId(StringNBT.class))) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("MusicDisc")));
            if (item instanceof MusicDiscItem) {
                return new TrackSourceMusicDisc((MusicDiscItem) item);
            }
        }
        
        return BROKEN_CASSETTE;
    }
    
    @Override
    protected CompoundNBT toNBT() {
        CompoundNBT nbt = super.toNBT();
        nbt.putString("MusicDisc", musicDisc.getRegistryName().toString());
        return nbt;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return musicDisc.getSound();
    }

    @Override
    protected String getTranslationKey(ResourceLocation trackId) {
        return musicDisc.getDescriptionId() + ".desc";
    }

}
