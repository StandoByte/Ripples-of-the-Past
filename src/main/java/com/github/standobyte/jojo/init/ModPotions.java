package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTION_TYPES, JojoMod.MOD_ID);
    
    public static final RegistryObject<Potion> FREEZE_POTION = POTIONS.register("freeze", 
            () -> new Potion(new EffectInstance(ModEffects.FREEZE.get(), 900, 0)));
    
    public static final RegistryObject<Potion> FREEZE_LONG_POTION = POTIONS.register("long_freeze", 
            () -> new Potion(new EffectInstance(ModEffects.FREEZE.get(), 2400, 0)));
    
    public static final RegistryObject<Potion> FREEZE_STRONG_POTION = POTIONS.register("strong_freeze", 
            () -> new Potion(new EffectInstance(ModEffects.FREEZE.get(), 450, 1)));
}
