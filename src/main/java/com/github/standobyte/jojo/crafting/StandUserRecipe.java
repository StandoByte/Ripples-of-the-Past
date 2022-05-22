package com.github.standobyte.jojo.crafting;

import com.github.standobyte.jojo.power.stand.IStandPower;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class StandUserRecipe<R extends ICraftingRecipe> extends PlayerPredicateRecipeWrapper<R> {
    private final NonNullList<ResourceLocation> stands;

    public StandUserRecipe(R recipe, NonNullList<ResourceLocation> stands) {
        super(recipe);
        this.stands = stands;
    }
    
    @Override
    protected boolean playerMatches(PlayerEntity player) {
        IStandPower standPower = IStandPower.getPlayerStandPower(player);
        return standPower.hasPower() && (stands.isEmpty() || stands.contains(standPower.getType().getRegistryName()));
    }

    public static class Serializer<R extends ICraftingRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<StandUserRecipe<R>> {
        private final IRecipeSerializer<R> wrappedRecipeSerializer;
        private final Factory<R> factory;
        
        public Serializer(IRecipeSerializer<R> wrappedRecipeSerializer, Factory<R> factory) {
            this.wrappedRecipeSerializer = wrappedRecipeSerializer;
            this.factory = factory;
        }

        @Override
        public StandUserRecipe<R> fromJson(ResourceLocation id, JsonObject json) {
            R recipe = wrappedRecipeSerializer.fromJson(id, json);
            NonNullList<ResourceLocation> standsList = NonNullList.create();
            JsonArray standsJson = json.getAsJsonArray("stand");
            standsJson.forEach(element -> {
                standsList.add(new ResourceLocation(element.getAsJsonObject().get("name").getAsString()));
            });
            return factory.create(recipe, standsList);
        }

        @Override
        public StandUserRecipe<R> fromNetwork(ResourceLocation id, PacketBuffer buf) {
            R recipe = wrappedRecipeSerializer.fromNetwork(id, buf);
            NonNullList<ResourceLocation> stands = NonNullList.create();
            int standsCount = buf.readVarInt();
            for (int i = 0; i < standsCount; i++) {
                stands.add(buf.readResourceLocation());
            }
            return factory.create(recipe, stands);
        }

        @Override
        public void toNetwork(PacketBuffer buf, StandUserRecipe<R> recipe) {
            wrappedRecipeSerializer.toNetwork(buf, recipe.recipe);
            
            buf.writeVarInt(recipe.stands.size());
            for (ResourceLocation resLoc : recipe.stands) {
                buf.writeResourceLocation(resLoc);
            }
        }
        
    }
    
    public static interface Factory<R extends ICraftingRecipe> {
        public StandUserRecipe<R> create(R recipe, NonNullList<ResourceLocation> stands);
    }

}
