package com.github.standobyte.jojo.itemtracking.itemcap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TrackerItemStackProvider implements ICapabilitySerializable<INBT>{
    @CapabilityInject(TrackerItemStack.class)
    public static Capability<TrackerItemStack> CAPABILITY = null;
    private LazyOptional<TrackerItemStack> instance;
    
    public TrackerItemStackProvider(ItemStack itemStack) {
        this.instance = LazyOptional.of(() -> new TrackerItemStack(itemStack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Capability LazyOptional is not attached.")), null, nbt);
    }
}
