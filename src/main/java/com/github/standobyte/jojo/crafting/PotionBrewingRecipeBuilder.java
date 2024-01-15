package com.github.standobyte.jojo.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class PotionBrewingRecipeBuilder {
    private final Potion inputPotion;
    private final Item ingredient;
    private final Potion basePotion;
    private Potion longPotion = null;
    private Potion strongPotion = null;
    
    public PotionBrewingRecipeBuilder(Potion input, Item ingredient, Potion resultPotion) {
        if (ingredient == Items.REDSTONE || ingredient == Items.GLOWSTONE_DUST) {
            throw new IllegalArgumentException("Can't make a potion recipe with redstone or glowstone dust as an ingredient.");
        }
        this.inputPotion = input;
        this.ingredient = ingredient;
        this.basePotion = resultPotion;
    }
    
    public PotionBrewingRecipeBuilder withLongPotion(Potion longPotion) {
        this.longPotion = longPotion;
        return this;
    }
    
    public PotionBrewingRecipeBuilder withStrongPotion(Potion strongPotion) {
        this.strongPotion = strongPotion;
        return this;
    }
    
    public void register() {
        List<BrewingRecipe> recipes = new ArrayList<>();
        
        recipes.add(brewing(Items.POTION,           inputPotion, ingredient, basePotion));
        recipes.add(brewing(Items.SPLASH_POTION,    inputPotion, ingredient, basePotion));
        recipes.add(brewing(Items.LINGERING_POTION, inputPotion, ingredient, basePotion));
        
        if (longPotion != null) {
            recipes.add(brewing(Items.POTION,           basePotion, Items.REDSTONE, longPotion));
            recipes.add(brewing(Items.SPLASH_POTION,    basePotion, Items.REDSTONE, longPotion));
            recipes.add(brewing(Items.LINGERING_POTION, basePotion, Items.REDSTONE, longPotion));
        }
        
        if (strongPotion != null) {
            recipes.add(brewing(Items.POTION,           basePotion, Items.GLOWSTONE_DUST, strongPotion));
            recipes.add(brewing(Items.SPLASH_POTION,    basePotion, Items.GLOWSTONE_DUST, strongPotion));
            recipes.add(brewing(Items.LINGERING_POTION, basePotion, Items.GLOWSTONE_DUST, strongPotion));
        }
        
        for (IBrewingRecipe recipe : recipes) {
            BrewingRecipeRegistry.addRecipe(recipe);
        }
    }
    
    private static BrewingRecipe brewing(Item potionItem, Potion inputPotion, Item ingredient, Potion outputPotion) {
        return new BrewingRecipe(
                new PotionIngredient(potionItem, inputPotion),  
                Ingredient.of(ingredient), 
                PotionUtils.setPotion(new ItemStack(potionItem), outputPotion));
    }
}
