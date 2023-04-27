package com.github.standobyte.jojo.crafting;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.capability.item.cassette.CassetteCap;
import com.github.standobyte.jojo.capability.item.cassette.CassetteCapProvider;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModRecipeSerializers;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public class CassetteCopyRecipe extends SpecialRecipe {
    
    public CassetteCopyRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CASSETTE_COPY.get();
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        Pair<ItemStack, Integer> result = originalRecordingAndCopyCount(inventory);
        return result != null && !result.getLeft().isEmpty() && result.getRight() > 0;
    }

    @Override
    public ItemStack assemble(CraftingInventory inventory) {
        Pair<ItemStack, Integer> result = originalRecordingAndCopyCount(inventory);
        if (result != null && !result.getLeft().isEmpty() && result.getRight() > 0) {
            LazyOptional<CassetteCap> cassetteCap = result.getLeft().getCapability(CassetteCapProvider.CAPABILITY);
            if (cassetteCap.isPresent()) {
                CassetteCap originalRecording = cassetteCap.resolve().get();
                if (originalRecording.getGeneration() < CassetteCap.MAX_GENERATION) {
                    ItemStack copies = new ItemStack(ModItems.CASSETTE_RECORDED.get(), result.getRight());
                    copies.getCapability(CassetteCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.copyFrom(originalRecording);
                        cap.incGeneration();
                    });
                    return copies;
                }
            }
        }
        
        return ItemStack.EMPTY;
    }

    @Nullable
    private Pair<ItemStack, Integer> originalRecordingAndCopyCount(CraftingInventory craftingGrid) {
        int blankCassettes = 0;
        ItemStack originalCassette = ItemStack.EMPTY;

        for (int i = 0; i < craftingGrid.getContainerSize(); ++i) {
            ItemStack item = craftingGrid.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem() == ModItems.CASSETTE_RECORDED.get()) {
                    if (!originalCassette.isEmpty()) {
                        return null;
                    }

                    originalCassette = item;
                } else {
                    if (item.getItem() != ModItems.CASSETTE_BLANK.get()) {
                        return null;
                    }

                    ++blankCassettes;
                }
            }
        }

        return Pair.of(originalCassette, blankCassettes);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inventory) {
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < items.size(); ++i) {
            ItemStack item = inventory.getItem(i);
            if (item.hasContainerItem()) {
                items.set(i, item.getContainerItem());
            } else if (item.getItem() == ModItems.CASSETTE_RECORDED.get()) {
                ItemStack originalRecordingItem = item.copy();
                originalRecordingItem.setCount(1);
                items.set(i, originalRecordingItem);
                break;
            }
        }

        return items;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }
}
