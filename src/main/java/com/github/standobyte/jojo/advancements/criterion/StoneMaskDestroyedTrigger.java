package com.github.standobyte.jojo.advancements.criterion;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class StoneMaskDestroyedTrigger extends AbstractCriterionTrigger<StoneMaskDestroyedTrigger.Instance> {
    private final ResourceLocation id;

    public StoneMaskDestroyedTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, Block stoneMaskBlock, ItemStack itemUsed, ItemStack stoneMaskItem) {
        trigger(player, (criterion) -> {
            return criterion.matches(stoneMaskBlock, itemUsed, stoneMaskItem);
        });
    }

    public StoneMaskDestroyedTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate playerPredicate, 
            ConditionArrayParser conditionArrayParser) {
        Block block = deserializeBlock(json);
        ItemPredicate itemUsed = ItemPredicate.fromJson(json.get("item_used"));
        ItemPredicate stoneMaskItem = ItemPredicate.fromJson(json.get("stone_mask_item"));
        return new StoneMaskDestroyedTrigger.Instance(id, playerPredicate, block, itemUsed, stoneMaskItem);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject json) {
        if (json.has("block")) {
            ResourceLocation resLoc = new ResourceLocation(JSONUtils.getAsString(json, "block"));
            return Optional.ofNullable(((ForgeRegistry<Block>) ForgeRegistries.BLOCKS)
                    .getRaw(resLoc)).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown block type '" + resLoc + "'");
                    });
        }
        else {
            return null;
        }
    }

    public static class Instance extends CriterionInstance {
        @Nullable
        private final Block block;
        private final ItemPredicate itemUsed;
        private final ItemPredicate stoneMaskItem;

        public Instance(ResourceLocation criterion, EntityPredicate.AndPredicate player, @Nullable Block block, 
                ItemPredicate itemUsed, ItemPredicate stoneMaskItem) {
            super(criterion, player);
            this.block = block;
            this.itemUsed = itemUsed;
            this.stoneMaskItem = stoneMaskItem;
        }

        public boolean matches(Block block, ItemStack itemUsed, ItemStack stoneMaskItem) {
            return (this.block == null || this.block == block)
                    && this.itemUsed.matches(itemUsed) && this.stoneMaskItem.matches(stoneMaskItem);
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            if (this.block != null) {
                jsonobject.addProperty("block", ForgeRegistries.BLOCKS.getKey(block).toString());
            }
            jsonobject.add("item_used", itemUsed.serializeToJson());
            jsonobject.add("stone_mask_item", stoneMaskItem.serializeToJson());
            return jsonobject;
        }
    }

}
