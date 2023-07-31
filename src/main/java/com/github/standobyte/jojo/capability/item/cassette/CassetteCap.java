package com.github.standobyte.jojo.capability.item.cassette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.item.cassette.TrackSource.TrackSourceType;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.CassetteSide;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

// FIXME !! (cassette) crazy diamond clearing the cassettes for lulz
public class CassetteCap {
    @Nonnull private TrackSourceList tracks = TrackSourceList.BROKEN_CASSETTE;
    private int generation = 0;
    @Nonnull private Optional<DyeColor> color = Optional.empty();
    
    private CassetteSide side = CassetteSide.SIDE_A;
    private int sideTrack = 0;
    
    public CassetteCap(ItemStack cassetteItem) {}
    

    
    public void recordTracks(List<TrackSource> tracks) {
        this.tracks = new TrackSourceList(tracks);
    }
    
    public TrackSourceList getTracks() {
        return tracks;
    }
    
    public void copyFrom(CassetteCap original) {
        this.tracks = original.tracks;
        this.generation = original.generation;
        this.color = original.color;
    }

    
    
    public int getGeneration() {
        return generation;
    }
    
    public static final int MAX_GENERATION = 4;
    public void incGeneration() {
        generation++;
    }
    
    
    

    public void setDye(@Nullable DyeColor dye) {
        this.color = Optional.ofNullable(dye);
    }
    
    public Optional<DyeColor> getDye() {
        return color;
    }
    
    
    
    public void setSide(CassetteSide side) {
        this.side = side;
    }
    
    public CassetteSide getSide() {
        return side;
    }
    
    public void setTrackOn(int sideTrack) {
        this.sideTrack = sideTrack;
    }
    
    public int getTrackOn() {
        return this.sideTrack;
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        nbt.put("Tracks", tracks.toNBT());
        nbt.putByte("Generation", (byte)generation);
        if (color.isPresent()) nbt.putByte("Dye", (byte) color.get().ordinal());
        nbt.putBoolean("Side", side == CassetteSide.SIDE_B);
        nbt.putByte("TrackNumber", (byte) sideTrack);
        
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("Tracks", MCUtil.getNbtId(ListNBT.class))) tracks = TrackSourceList.fromNBT(nbt.getList("Tracks", MCUtil.getNbtId(CompoundNBT.class)));
        generation = nbt.getInt("Generation");
        color = getColor(nbt, "Dye");
        side = nbt.getBoolean("Side") ? CassetteSide.SIDE_B : CassetteSide.SIDE_A;
        sideTrack = nbt.getByte("TrackNumber");
    }
    
    private Optional<DyeColor> getColor(CompoundNBT nbt, String nbtKey) {
        if (nbt.contains(nbtKey, MCUtil.getNbtId(ByteNBT.class))) {
            int ordinal = nbt.getByte(nbtKey);
            DyeColor[] colors = DyeColor.values();
            if (ordinal >= 0 && ordinal < colors.length) {
                return Optional.of(colors[ordinal]);
            }
        }
        return Optional.empty();
    }
    
    
    
    public static class TrackSourceList {
        public static final TrackSourceList BROKEN_CASSETTE = new TrackSourceList(Collections.emptyList());
        private final List<TrackSource> tracks;

        private TrackSourceList(List<TrackSource> tracks) {
            this.tracks = Collections.unmodifiableList(tracks);
        }

        public Stream<TrackSource> getTracks() {
            return tracks.stream();
        }

        public boolean isBroken() {
            return this == BROKEN_CASSETTE;
        }
        
        private ListNBT toNBT() {
            ListNBT tracksNBT = new ListNBT();
            for (TrackSource track : tracks) {
                tracksNBT.add(track.toNBT());
            }
            return tracksNBT;
        }
        
        private static TrackSourceList fromNBT(ListNBT nbt) {
            if (nbt == null || nbt.isEmpty()) return BROKEN_CASSETTE;
            
            List<TrackSource> tracks = new ArrayList<>();
            for (int i = 0; i < nbt.size(); i++) {
                TrackSource trackSource = TrackSourceType.fromNBT(nbt.getCompound(i));
                if (trackSource == TrackSource.BROKEN_CASSETTE) {
                    return TrackSourceList.BROKEN_CASSETTE;
                }
                tracks.add(trackSource);
            }
            if (tracks.isEmpty()) return TrackSourceList.BROKEN_CASSETTE;

            return new TrackSourceList(tracks);
        }
    }
}
