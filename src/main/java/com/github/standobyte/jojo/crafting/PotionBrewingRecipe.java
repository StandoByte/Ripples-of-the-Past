package com.github.standobyte.jojo.crafting;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class PotionBrewingRecipe implements IBrewingRecipe {
    private final Predicate<Potion> inputPotion;
    private final Predicate<ItemStack> ingredient;
    private final Potion basePotion;
    private Potion longPotion = null;
    private Potion strongPotion = null;

    public PotionBrewingRecipe(Potion input, Item ingredient, Potion resultPotion) {
        if (ingredient == Items.REDSTONE || ingredient == Items.GLOWSTONE_DUST) {
            throw new IllegalArgumentException("Can't make a potion recipe with redstone or glowstone dust as an ingredient.");
        }
        this.inputPotion = potion -> potion.equals(input);
        this.ingredient = stack -> stack.getItem().equals(ingredient);
        this.basePotion = resultPotion;
    }
    
    public PotionBrewingRecipe withLongPotion(Potion longPotion) {
        this.longPotion = longPotion;
        return this;
    }
    
    public PotionBrewingRecipe withStrongPotion(Potion strongPotion) {
        this.strongPotion = strongPotion;
        return this;
    }

    @Override
    public boolean isInput(ItemStack input) {
        if (!(input.getItem() instanceof PotionItem)) return false;
        Potion inputtedPotion = PotionUtils.getPotion(input);
        return this.inputPotion.test(inputtedPotion) || 
                this.basePotion.equals(inputtedPotion);
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return this.ingredient.test(ingredient) || 
                longPotion != null && ingredient.getItem() == Items.REDSTONE || 
                strongPotion != null && ingredient.getItem() == Items.GLOWSTONE_DUST;
    }
    
    @Nullable
    private Potion basePotion(Potion inputPotion, ItemStack ingredient) {
        return this.inputPotion.test(inputPotion) && this.ingredient.test(ingredient) ? basePotion : null;
    }

    @Nullable
    private Potion longPotion(Potion inputPotion, ItemStack ingredient) {
        return this.basePotion.equals(inputPotion) && ingredient.getItem() == Items.REDSTONE ? longPotion : null;
    }
    
    @Nullable
    private Potion strongPotion(Potion inputPotion, ItemStack ingredient) {
        return this.basePotion.equals(inputPotion) && ingredient.getItem() == Items.GLOWSTONE_DUST ? strongPotion : null;
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (input.getItem() instanceof PotionItem) {
            Potion inputPotion = PotionUtils.getPotion(input);
            
            Potion outputPotion = basePotion(inputPotion, ingredient);
            if (outputPotion == null) outputPotion = longPotion(inputPotion, ingredient);
            if (outputPotion == null) outputPotion = strongPotion(inputPotion, ingredient);
            
            if (outputPotion != null) {
                return PotionUtils.setPotion(input.copy(), outputPotion);
            }
        }
        return ItemStack.EMPTY;
    }

}
