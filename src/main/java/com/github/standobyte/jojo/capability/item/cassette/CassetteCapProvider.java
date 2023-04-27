package com.github.standobyte.jojo.capability.item.cassette;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CassetteCapProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(CassetteCap.class)
    public static Capability<CassetteCap> CAPABILITY = null;
    private LazyOptional<CassetteCap> instance;
    
    public CassetteCapProvider(ItemStack itemStack, @Nullable CompoundNBT nbt) {
        CassetteCap cap = new CassetteCap(itemStack);
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
                () -> new IllegalArgumentException("Cassette item capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Cassette item capability LazyOptional is not attached.")), null, nbt);
    }

}
