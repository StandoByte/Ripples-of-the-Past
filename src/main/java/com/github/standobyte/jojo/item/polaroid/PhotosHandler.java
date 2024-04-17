package com.github.standobyte.jojo.item.polaroid;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundNBT;

public class PhotosHandler {
    private Int2ObjectMap<PolaroidPhotoData> photos = new Int2ObjectArrayMap<>();
    private IntSet requested = new IntArraySet();
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        photos.forEach((key, photo) -> {
            nbt.put(String.valueOf(key), photo.toNBT());
        });
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        nbt.getAllKeys().forEach(key -> {
            try {
                int keyInt = Integer.parseInt(key);
                PolaroidPhotoData photo = PolaroidPhotoData.fromNBT(nbt.get(key));
                if (photo != null) {
                    photos.put(keyInt, photo);
                }
            }
            catch (NumberFormatException e) {
                
            }
        });
    }
}
