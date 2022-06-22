package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.entity.item.PaintingType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModPaintings {
    public static final DeferredRegister<PaintingType> PAINTINGS = DeferredRegister.create(ForgeRegistries.PAINTING_TYPES, JojoMod.MOD_ID);
    
    public static final RegistryObject<PaintingType> MONA_LISA = PAINTINGS.register("mona_lisa", 
            () -> new PaintingType(32, 48));
    
    public static final RegistryObject<PaintingType> MONA_LISA_HANDS = PAINTINGS.register("hands", 
            () -> new PaintingType(16, 16));

}
