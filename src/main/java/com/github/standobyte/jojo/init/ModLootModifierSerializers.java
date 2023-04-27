package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.loot.AdditionalSingleItemLootModifier;
import com.github.standobyte.jojo.util.mc.loot.BlockStateLootTracker;
import com.github.standobyte.jojo.util.mc.loot.ReplaceItemNbtModifier;

import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModLootModifierSerializers {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIER_SERIALIZERS = 
            DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, JojoMod.MOD_ID);
    
    public static final RegistryObject<AdditionalSingleItemLootModifier.Serializer> ADDITIONAL_SINGLE_ITEM = LOOT_MODIFIER_SERIALIZERS
            .register("additional_single_item", AdditionalSingleItemLootModifier.Serializer::new);
    
    public static final RegistryObject<ReplaceItemNbtModifier.Serializer> REPLACE_ITEM_NBT = LOOT_MODIFIER_SERIALIZERS
            .register("replace_item_nbt", ReplaceItemNbtModifier.Serializer::new);
    
    public static final RegistryObject<BlockStateLootTracker.Serializer> BLOCK_LOOT_TRACKER = LOOT_MODIFIER_SERIALIZERS
            .register("block_loot_tracker", BlockStateLootTracker.Serializer::new);
}
