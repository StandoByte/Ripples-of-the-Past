package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.crafting.StandUserRecipe;
import com.github.standobyte.jojo.crafting.StandUserShapedRecipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipeSerializers {
    public static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, JojoMod.MOD_ID);

    public static final RegistryObject<IRecipeSerializer<StandUserRecipe<ShapedRecipe>>> STAND_USER_SHAPED_RECIPE = SERIALIZERS.register("crafting_shaped_stand", 
            () -> new StandUserRecipe.Serializer<>(IRecipeSerializer.SHAPED_RECIPE, StandUserShapedRecipe::new));
}
