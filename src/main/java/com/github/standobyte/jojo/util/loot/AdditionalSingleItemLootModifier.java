package com.github.standobyte.jojo.util.loot;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class AdditionalSingleItemLootModifier extends LootModifier {
    private final Item additionalItem;

    public AdditionalSingleItemLootModifier(ILootCondition[] conditions, Item additionalItem) {
        super(conditions);
        this.additionalItem = additionalItem;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(new ItemStack(additionalItem));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AdditionalSingleItemLootModifier> {

        @Override
        public AdditionalSingleItemLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
            Item additionalItem = JSONUtils.getAsItem(object, "additional_item");
            return new AdditionalSingleItemLootModifier(conditions, additionalItem);
        }

        @Override
        public JsonObject write(AdditionalSingleItemLootModifier instance) {
            JsonObject json = makeConditions(instance.conditions);
            json.addProperty("additional_item", ForgeRegistries.ITEMS.getKey(instance.additionalItem).toString());
            return json;
        }
        
    }
}
