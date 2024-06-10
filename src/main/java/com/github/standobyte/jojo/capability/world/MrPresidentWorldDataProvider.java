package com.github.standobyte.jojo.capability.world;

import com.github.standobyte.jojo.world.dimension.mr_president.MrPresidentWorldData;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MrPresidentWorldDataProvider implements ICapabilitySerializable<INBT>{
    @CapabilityInject(MrPresidentWorldData.class)
    public static Capability<MrPresidentWorldData> CAPABILITY = null;
    private LazyOptional<MrPresidentWorldData> instance;
    
    public MrPresidentWorldDataProvider(ServerWorld world) {
        this.instance = LazyOptional.of(() -> new MrPresidentWorldData(world));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Mr.President capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Mr.President capability LazyOptional is not attached.")), null, nbt);
    }
}
