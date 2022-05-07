package com.github.standobyte.jojo.crafting;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.reflection.CommonReflection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class PlayerConditionCraftingRecipe extends SpecialRecipe {
    private final Predicate<PlayerEntity> condition;

    public PlayerConditionCraftingRecipe(ResourceLocation id, Predicate<PlayerEntity> condition) {
        super(id);
        this.condition = condition;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        if (inventoryMatches(inventory, world)) {
            PlayerEntity player = getPlayer(inventory);
            return player != null ? condition.test(player) : false;
        }
        return false;
    }
    
    protected abstract boolean inventoryMatches(CraftingInventory inventory, World world);

    @Nullable
    protected final PlayerEntity getPlayer(CraftingInventory inventory) {
        PlayerEntity player = null;
        Container menu = CommonReflection.getCraftingInventoryMenu(inventory);
        if (menu instanceof PlayerContainer) {
            player = CommonReflection.getPlayer((PlayerContainer) menu);
        }
        else if (menu instanceof WorkbenchContainer) {
            player = CommonReflection.getPlayer((WorkbenchContainer) menu);
        }
        return player;
    }
}
