package com.github.standobyte.jojo.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GumItem extends Item {

    public GumItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (stack.getItem().isEdible()) {
            return 128;
        } else {
            return 0;
        }
    }
}
