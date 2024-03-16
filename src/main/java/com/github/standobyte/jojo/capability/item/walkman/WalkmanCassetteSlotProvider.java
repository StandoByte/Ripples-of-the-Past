package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class WalkmanCassetteSlotProvider implements ICapabilitySerializable<INBT> {
    private LazyOptional<IItemHandler> instance;
    
    public WalkmanCassetteSlotProvider(ItemStack itemStack) {
        this.instance = LazyOptional.of(() -> new WalkmanCassetteSlotCap(itemStack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (!(nbt instanceof ListNBT)) return;
        
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")), null, nbt);
    }

}
