package com.github.standobyte.jojo.item.polaroid;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

public class PolaroidPhotoData {

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        return nbt;
    }
    
    @Nullable
    public static PolaroidPhotoData fromNBT(INBT inbt) {
        if (inbt instanceof CompoundNBT) {
            CompoundNBT nbt = (CompoundNBT) inbt;
            
        }
        return null;
    }

}
