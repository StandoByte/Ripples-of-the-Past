package com.github.standobyte.jojo.util.mc.loot;

import java.util.List;

import com.github.standobyte.jojo.util.mc.MCUtil;
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

public class ReplaceItemNbtModifier extends LootModifier {
    private final Item item;
    private final CompoundNBT tagToReplace;
    private final CompoundNBT replacingTag;

    public ReplaceItemNbtModifier(ILootCondition[] conditions, Item item, CompoundNBT tagToReplace, CompoundNBT replacingTag) {
        super(conditions);
        this.item = item;
        this.tagToReplace = tagToReplace;
        this.replacingTag = replacingTag;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.forEach(stack -> {
            if (stack.getItem() == item) {
                MCUtil.replaceNbtValues(stack.getOrCreateTag(), tagToReplace, replacingTag);
            }
        });
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<ReplaceItemNbtModifier> {

        @Override
        public ReplaceItemNbtModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
            JsonObject entryReplacement = JSONUtils.getAsJsonObject(object, "replace_nbt");
            Item item = JSONUtils.getAsItem(entryReplacement, "item");
            try {
                CompoundNBT tagToReplace = JsonToNBT.parseTag(JSONUtils.getAsString(entryReplacement, "to_replace"));
                CompoundNBT replacingTag = JsonToNBT.parseTag(JSONUtils.getAsString(entryReplacement, "replace_with"));
                return new ReplaceItemNbtModifier(conditions, item, tagToReplace, replacingTag);
            } 
            catch (CommandSyntaxException commandSyntaxException) {
                throw new JsonSyntaxException(commandSyntaxException.getMessage());
            }
        }

        @Override
        public JsonObject write(ReplaceItemNbtModifier instance) {
            JsonObject json = makeConditions(instance.conditions);
            JsonObject entryReplacement = new JsonObject();
            entryReplacement.addProperty("item", ForgeRegistries.ITEMS.getKey(instance.item).toString());
            entryReplacement.addProperty("to_replace", instance.tagToReplace.toString());
            entryReplacement.addProperty("replace_with", instance.replacingTag.toString());
            json.add("replace_nbt", entryReplacement);
            return json;
        }
        
    }

}
