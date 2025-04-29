package com.github.standobyte.jojo.world.dimension;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ModDimensions {
    public static RegistryKey<World> MR_PRESIDENT;
    
    public static void init() {
        MR_PRESIDENT = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(JojoMod.MOD_ID, "mr_president"));
    }
}
