package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;

public class CrazyDiamondDecompose extends StandAction {

    public CrazyDiamondDecompose(AbstractBuilder<?> builder) {
        super(builder);
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide() && user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;
            ItemStack heldItem = user.getOffhandItem();
            if (!heldItem.isEmpty()) {
                List<ICraftingRecipe> recipes = new ArrayList<>();
                for (IRecipe<?> recipe : world.getRecipeManager().getRecipes()) {
                    if (recipe instanceof ICraftingRecipe && recipe.canCraftInDimensions(3, 3)
                            && !recipe.getIngredients().isEmpty() && matches(heldItem, recipe.getResultItem())) {
                        recipes.add((ICraftingRecipe) recipe);
                    }
                }
                
                if (!recipes.isEmpty()) {
                    ICraftingRecipe randomRecipe = recipes.get(user.getRandom().nextInt(recipes.size()));
                    ItemStack[] ingredients = getIngredients(randomRecipe);
                    boolean gaveIngredients = false;
                    for (ItemStack ingredient : ingredients) {
                        if (!ingredient.isEmpty()) {
                            giveToPlayer(player, ingredient);
                            gaveIngredients = true;
                        }
                    }
                    if (gaveIngredients) {
                        heldItem.shrink(randomRecipe.getResultItem().getCount());
                    }
                }
            }
        }
    }
    
    private void giveToPlayer(PlayerEntity player, ItemStack itemStack) {
        if (player.inventory.add(itemStack) && itemStack.isEmpty()) {
            player.inventoryMenu.broadcastChanges();
        } else {
            ItemEntity itementity = player.drop(itemStack, false);
            if (itementity != null) {
                itementity.setNoPickUpDelay();
                itementity.setOwner(player.getUUID());
            }
        }
    }

    private static boolean matches(ItemStack input, ItemStack output) {
        return input.getItem() == output.getItem() && input.getCount() >= output.getCount();
    }

    private ItemStack[] getIngredients(ICraftingRecipe recipe) {
        List<Ingredient> ingredients = recipe.getIngredients();
        ItemStack[] stacks = new ItemStack[ingredients.size()];

        Random random = new Random();
        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack[] matchingStacks = ingredients.get(i).getItems();
            stacks[i] = matchingStacks.length > 0 ? matchingStacks[random.nextInt(matchingStacks.length)].copy() : ItemStack.EMPTY;
        }

        return stacks;
    }
}
