package com.github.standobyte.jojo.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;

public class FixedCraftingResultSlot<C extends CraftingInventory, T extends IRecipe<C>> extends CraftingResultSlot {
    protected final IRecipeType<T> recipeType;
    protected final C craftSlots;
    protected final PlayerEntity player;

    public FixedCraftingResultSlot(PlayerEntity pPlayer, C pCraftSlots, 
            IInventory pContainer, int pSlot, int pXPosition, int pYPosition, IRecipeType<T> recipeType) {
        super(pPlayer, pCraftSlots, pContainer, pSlot, pXPosition, pYPosition);
        this.recipeType = recipeType;
        this.craftSlots = pCraftSlots;
        this.player = pPlayer;
    }
    
    @Override
    public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
        checkTakeAchievements(pStack);
        ForgeHooks.setCraftingPlayer(pPlayer);
        NonNullList<ItemStack> nonnulllist = pPlayer.level.getRecipeManager().getRemainingItemsFor(recipeType, craftSlots, pPlayer.level);
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = craftSlots.getItem(i);
            ItemStack itemstack1 = nonnulllist.get(i);
            if (!itemstack.isEmpty()) {
                craftSlots.removeItem(i, 1);
                itemstack = craftSlots.getItem(i);
            }

            if (!itemstack1.isEmpty()) {
                if (itemstack.isEmpty()) {
                    craftSlots.setItem(i, itemstack1);
                } else if (ItemStack.isSame(itemstack, itemstack1) && ItemStack.tagMatches(itemstack, itemstack1)) {
                    itemstack1.grow(itemstack.getCount());
                    craftSlots.setItem(i, itemstack1);
                } else if (!player.inventory.add(itemstack1)) {
                    player.drop(itemstack1, false);
                }
            }
        }

        return pStack;
    }
    
}
