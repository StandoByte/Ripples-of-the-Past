package com.github.standobyte.jojo.capability.item.walkman;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.container.WalkmanItemContainer;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.ItemStackHandler;

public class WalkmanCassetteSlotCap extends ItemStackHandler implements INamedContainerProvider {
    private final ItemStack walkmanItem;

    public WalkmanCassetteSlotCap(ItemStack walkmanItem) {
        super(1);
        this.walkmanItem = walkmanItem;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() == ModItems.CASSETTE_RECORDED.get();
    }

    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new WalkmanItemContainer(id, inventory, this, walkmanItem);
    }

    @Override
    public ITextComponent getDisplayName() {
        return StringTextComponent.EMPTY;
    }
}
