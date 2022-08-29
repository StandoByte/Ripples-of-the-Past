package com.github.standobyte.jojo.capability.chunk;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ChunkCapProvider implements ICapabilitySerializable<INBT>{
    @CapabilityInject(ChunkCap.class)
    public static Capability<ChunkCap> CAPABILITY = null;
    private LazyOptional<ChunkCap> instance;
    
    public ChunkCapProvider(Chunk chunk) {
        this.instance = LazyOptional.of(() -> new ChunkCap(chunk));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Chunk capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Chunk capability LazyOptional is not attached.")), null, nbt);
    }

}
