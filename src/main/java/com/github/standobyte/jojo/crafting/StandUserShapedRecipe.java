package com.github.standobyte.jojo.crafting;

import com.github.standobyte.jojo.init.ModRecipeSerializers;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class StandUserShapedRecipe extends StandUserRecipe<ShapedRecipe> {

    public StandUserShapedRecipe(ShapedRecipe recipe, NonNullList<ResourceLocation> stands) {
        super(recipe, stands);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STAND_USER_SHAPED_RECIPE.get();
    }

}
