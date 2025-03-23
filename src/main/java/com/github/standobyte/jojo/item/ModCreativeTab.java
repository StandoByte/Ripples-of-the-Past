package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.client.render.item.CustomIconItem;
import com.github.standobyte.jojo.init.ModEnchantments;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModCreativeTab extends ItemGroup {

    public ModCreativeTab(String label) {
        super(label);
        this.setEnchantmentCategories(new EnchantmentType[]{ ModEnchantments.STAND_ARROW, ModEnchantments.GLOVES });
    }

    @Override
    public ItemStack makeIcon() {
        return CustomIconItem.makeIconItem(CustomIconItem.CustomModelIcon.MOD_LOGO);
    }

}
