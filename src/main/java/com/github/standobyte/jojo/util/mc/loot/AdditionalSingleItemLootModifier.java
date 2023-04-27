package com.github.standobyte.jojo.util.mc.loot;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class AdditionalSingleItemLootModifier extends LootModifier {
    private final ItemStack additionalItem;

    public AdditionalSingleItemLootModifier(ILootCondition[] conditions, ItemStack additionalItem) {
        super(conditions);
        this.additionalItem = additionalItem;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(additionalItem.copy());
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AdditionalSingleItemLootModifier> {

        @Override
        public AdditionalSingleItemLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
            JsonObject itemObject = JSONUtils.getAsJsonObject(object, "additional_item");
            
            Item additionalItem = JSONUtils.getAsItem(itemObject, "name");
            ItemStack itemStack = new ItemStack(additionalItem);
            if (itemObject.has("nbt")) {
                try {
                    CompoundNBT nbt = JsonToNBT.parseTag(JSONUtils.convertToString(itemObject.get("nbt"), "nbt"));
                    itemStack.setTag(nbt);
                } catch (CommandSyntaxException commandsyntaxexception) {
                    throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
                }
            }
            
            return new AdditionalSingleItemLootModifier(conditions, itemStack);
        }

        @Override
        public JsonObject write(AdditionalSingleItemLootModifier instance) {
            JsonObject json = makeConditions(instance.conditions);
            JsonObject itemObject = new JsonObject();
            
            itemObject.addProperty("name", ForgeRegistries.ITEMS.getKey(instance.additionalItem.getItem()).toString());
            if (instance.additionalItem.hasTag()) {
                itemObject.addProperty("nbt", instance.additionalItem.getTag().toString());
            }
            
            json.add("additional_item", itemObject);
            return json;
        }
        
    }
}
