package com.github.standobyte.jojo.crafting;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class PlayerPredicateRecipeWrapper<R extends ICraftingRecipe> implements ICraftingRecipe {
    protected final R recipe;
    
    protected PlayerPredicateRecipeWrapper(R recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        return recipe.matches(inventory, world) && playerMatches(getPlayer(inventory));
    }
    
    protected abstract boolean playerMatches(PlayerEntity player);

    @Override
    public ItemStack assemble(CraftingInventory inventory) {
        return recipe.assemble(inventory);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return recipe.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem() {
        return recipe.getResultItem();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipe.getIngredients();
    }

    @Override
    public ResourceLocation getId() {
        return recipe.getId();
    }
    
    @Nullable
    private static PlayerEntity getPlayer(CraftingInventory inventory) {
        PlayerEntity player = null;
        Container menu = CommonReflection.getCraftingInventoryMenu(inventory);
        if (menu instanceof PlayerContainer) {
            player = CommonReflection.getPlayer((PlayerContainer) menu);
        }
        else if (menu instanceof WorkbenchContainer) {
            player = CommonReflection.getPlayer((WorkbenchContainer) menu);
        }
        return player;
    }

}
