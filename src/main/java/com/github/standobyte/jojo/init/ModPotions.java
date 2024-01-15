package com.github.standobyte.jojo.init;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.crafting.PotionBrewingRecipeBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTION_TYPES, JojoMod.MOD_ID);
    
    public static final RegistryObject<Potion> FREEZE_POTION = POTIONS.register("freeze", 
            () -> new Potion(new EffectInstance(ModStatusEffects.FREEZE.get(), 900, 0)));
    
    public static final RegistryObject<Potion> FREEZE_LONG_POTION = POTIONS.register("long_freeze", 
            () -> new Potion(new EffectInstance(ModStatusEffects.FREEZE.get(), 2400, 0)));
    
    public static final RegistryObject<Potion> FREEZE_STRONG_POTION = POTIONS.register("strong_freeze", 
            () -> new Potion(new EffectInstance(ModStatusEffects.FREEZE.get(), 450, 1)));
    
    
    public static void registerRecipes() {
        registerRecipes(Potions.AWKWARD, Items.BLUE_ICE, FREEZE_POTION.get(), FREEZE_LONG_POTION.get(), FREEZE_STRONG_POTION.get());
    }
    
    private static void registerRecipes(Potion initialPotion, Item initialIngredient, 
            Potion basePotion, @Nullable Potion longPotion, @Nullable Potion strongPotion) {
        new PotionBrewingRecipeBuilder(initialPotion, initialIngredient, basePotion)
        .withLongPotion(longPotion).withStrongPotion(strongPotion)
        .register();
    }
}
