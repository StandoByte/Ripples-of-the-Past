package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class WalkmanCapProvider implements ICapabilitySerializable<INBT> {
    private static Capability<IItemHandler> CAPABILITY = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    private LazyOptional<IItemHandler> instance;
    
    public WalkmanCapProvider(ItemStack itemStack) {
        this.instance = LazyOptional.of(() -> new WalkmanSlotHandlerCap(itemStack));
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
