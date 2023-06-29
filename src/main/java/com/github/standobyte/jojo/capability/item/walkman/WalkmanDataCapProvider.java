package com.github.standobyte.jojo.capability.item.walkman;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WalkmanDataCapProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(WalkmanDataCap.class)
    public static Capability<WalkmanDataCap> CAPABILITY = null;
    private LazyOptional<WalkmanDataCap> instance;
    
    public WalkmanDataCapProvider(ItemStack itemStack, @Nullable CompoundNBT nbt) {
        WalkmanDataCap cap = new WalkmanDataCap(itemStack);
        if (nbt != null) cap.fromNBT(nbt);
        this.instance = LazyOptional.of(() -> cap);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")), null, nbt);
    }

}
