package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public class WalkmanDataCap {
    private int id;
    private boolean idInitialized = false;
    private float volume = 1.0F;
    private PlaybackMode playbackMode = PlaybackMode.STOP_AT_THE_END;
    
    public WalkmanDataCap(ItemStack cassetteItem) {}
    
    public void initId(ServerWorld world) {
        if (!idInitialized) {
            id = SaveFileUtilCapProvider.getSaveFileCap(((ServerWorld) world).getServer()).incWalkmanId();
            idInitialized = true;
        }
    }
    
    public int getId() {
        if (!idInitialized) {
            throw new IllegalStateException("The walkman's id hasn't been initialized yet!");
        }
        return id;
    }
    
    public boolean isIdInitialized() {
        return idInitialized;
    }
    
    public boolean checkId(int id) {
        return idInitialized && this.id == id;
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setVolume(float volume) {
        this.volume = MathHelper.clamp(volume, 0, 1);
    }
    
    public PlaybackMode getPlaybackMode() {
        return playbackMode;
    }
    
    public void setPlaybackMode(PlaybackMode mode) {
        if (mode != null) {
            this.playbackMode = mode;
        }
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (idInitialized) nbt.putInt("Id", id);
        nbt.putFloat("Volume", volume);
        nbt.putBoolean("Loop", playbackMode == PlaybackMode.LOOP);
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("Id", MCUtil.getNbtId(IntNBT.class))) {
            id = nbt.getInt("Id");
            idInitialized = true;
        }
        else {
            idInitialized = false;
        }
        this.volume = MathHelper.clamp(nbt.getFloat("Volume"), 0, 1);
        this.playbackMode = nbt.getBoolean("Loop") ? PlaybackMode.LOOP : PlaybackMode.STOP_AT_THE_END;
    }
    
    
    
    public static enum PlaybackMode {
        STOP_AT_THE_END { @Override public PlaybackMode getOpposite() { return LOOP; }},
        LOOP { @Override public PlaybackMode getOpposite() { return STOP_AT_THE_END; }};
        
        public abstract PlaybackMode getOpposite();
    }
}
