package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.AdditionalSingleItemLootModifier;

import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModLootModifierSerializers {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIER_SERIALIZERS = 
            DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, JojoMod.MOD_ID);
    
    public static final RegistryObject<AdditionalSingleItemLootModifier.Serializer> JUNGLE_TEMPLE_STONE_MASK = LOOT_MODIFIER_SERIALIZERS.register("jungle_temple_stone_mask", 
            () -> new AdditionalSingleItemLootModifier.Serializer());
    
    public static final RegistryObject<AdditionalSingleItemLootModifier.Serializer> DESERT_PYRAMID_BEETLE_ARROW = LOOT_MODIFIER_SERIALIZERS.register("desert_pyramid_beetle_arrow", 
            () -> new AdditionalSingleItemLootModifier.Serializer());
    
    public static final RegistryObject<AdditionalSingleItemLootModifier.Serializer> REDSTONE_ORE_AJA_STONE = LOOT_MODIFIER_SERIALIZERS.register("redstone_ore_aja_stone", 
            () -> new AdditionalSingleItemLootModifier.Serializer());
    
    public static final RegistryObject<AdditionalSingleItemLootModifier.Serializer> REDSTONE_ORE_SUPER_AJA = LOOT_MODIFIER_SERIALIZERS.register("redstone_ore_super_aja", 
            () -> new AdditionalSingleItemLootModifier.Serializer());
}
