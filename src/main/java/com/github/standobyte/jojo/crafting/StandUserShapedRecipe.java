package com.github.standobyte.jojo.crafting;

import com.github.standobyte.jojo.init.ModRecipeSerializers;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class StandUserShapedRecipe extends StandUserRecipe<ShapedRecipe> implements IShapedRecipe<CraftingInventory> {

    public StandUserShapedRecipe(ShapedRecipe recipe, NonNullList<ResourceLocation> stands) {
        super(recipe, stands);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STAND_USER_SHAPED_RECIPE.get();
    }

    @Override
    public int getRecipeWidth() {
        return recipe.getRecipeWidth();
    }

    @Override
    public int getRecipeHeight() {
        return recipe.getRecipeHeight();
    }

}
