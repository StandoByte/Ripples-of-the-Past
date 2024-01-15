package com.github.standobyte.jojo.crafting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.crafting.NBTIngredient;

public class PotionIngredient extends NBTIngredient {
    
    public PotionIngredient(Item potionItem, Potion potion) {
        this(PotionUtils.setPotion(new ItemStack(potionItem), potion));
    }

    public PotionIngredient(ItemStack stack) {
        super(stack);
    }

}
