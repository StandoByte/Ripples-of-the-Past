package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class CrazyDiamondPreviousState extends StandEntityAction {

    public CrazyDiamondPreviousState(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public ActionConditionResult checkStandTarget(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        switch (target.getType()) {
        case BLOCK:
            if (standPower.getResolveLevel() >= 2) {
                // FIXME !! (prev state) block check
                return ActionConditionResult.POSITIVE;
            }
            return ActionConditionResult.NEGATIVE;
        case ENTITY:
            Entity targetEntity = target.getEntity();
            return standPower.getResolveLevel() >= 3 && (
                    targetEntity instanceof TNTEntity
                    || targetEntity.getType() == EntityType.SNOW_GOLEM ||
                    standPower.getResolveLevel() >= 4 && (
                            targetEntity.getType() == EntityType.IRON_GOLEM
                            || targetEntity.getType() == EntityType.WITHER && ((WitherEntity) targetEntity).getInvulnerableTicks() > 0))
                    ? ActionConditionResult.POSITIVE : conditionMessage("entity_revert");
        default:
            // FIXME !! (prev state) offhand item check
            return ActionConditionResult.POSITIVE;
        }
    }

    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        return ActionConditionResult.POSITIVE;
    }

    // FIXME ! (prev state) CD restore sound
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        ActionTarget target = task.getTarget();
        switch (target.getType()) {
        case ENTITY:
            if (userPower.getResolveLevel() >= 3) {
                Entity targetEntity = target.getEntity();

                if (targetEntity instanceof TNTEntity) {
                    TNTEntity tnt = (TNTEntity) targetEntity;
                    if (CrazyDiamondHeal.handle(world, tnt, tnt, e -> {
                        e.setFuse(task.getTick() == 0 ? e.getFuse() - e.tickCount + 2 : e.getFuse() + 2);
                    }, e -> e.getFuse() < 80)) {
                        CrazyDiamondHeal.handle(world, tnt, tnt, 
                                e -> {
                                    Block tntBlock = ForgeRegistries.BLOCKS.getValue(e.getType().getRegistryName());
                                    if (tntBlock == null || tntBlock.getRegistryName().equals(ForgeRegistries.BLOCKS.getDefaultKey())) {
                                        tntBlock = Blocks.TNT;
                                    }
                                    BlockPos blockPos = e.blockPosition();
                                    e.remove();
                                    world.setBlockAndUpdate(blockPos, tntBlock.defaultBlockState());
                                }, e -> true);
                    }
                    return;
                }

                else if (targetEntity.getType() == EntityType.SNOW_GOLEM) {
                    if (CrazyDiamondHeal.handleLivingEntity(world, (LivingEntity) targetEntity)) {
                        CrazyDiamondHeal.handle(world, targetEntity, targetEntity, 
                                e -> {
                                    BlockPos blockPos = e.blockPosition();
                                    world.setBlockAndUpdate(blockPos.offset(0, 2, 0), Blocks.CARVED_PUMPKIN.defaultBlockState());
                                    world.setBlockAndUpdate(blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
                                    world.setBlockAndUpdate(blockPos.offset(0, 1, 0), Blocks.SNOW_BLOCK.defaultBlockState());
                                    e.remove();
                                }, e -> true);
                    }
                    return;
                }

                else if (userPower.getResolveLevel() >= 4) {
                    if (targetEntity.getType() == EntityType.IRON_GOLEM) {
                        if (CrazyDiamondHeal.handleLivingEntity(world, (LivingEntity) targetEntity)) {
                            CrazyDiamondHeal.handle(world, targetEntity, targetEntity, 
                                    e -> {
                                        BlockPos blockPos = e.blockPosition();
                                        world.setBlockAndUpdate(blockPos, Blocks.IRON_BLOCK.defaultBlockState());
                                        world.setBlockAndUpdate(blockPos.offset(0, 2, 0), Blocks.CARVED_PUMPKIN.defaultBlockState());
                                        world.setBlockAndUpdate(blockPos.offset(0, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
                                        world.setBlockAndUpdate(blockPos.offset(1, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
                                        world.setBlockAndUpdate(blockPos.offset(-1, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
                                        e.remove();
                                    }, e -> true);
                        }
                        return;
                    }

                    else if (targetEntity.getType() == EntityType.WITHER) {
                        WitherEntity wither = (WitherEntity) targetEntity;
                        int spawnTicks = wither.getInvulnerableTicks();
                        if (spawnTicks > 0) {
                            if (CrazyDiamondHeal.handle(world, wither, wither, 
                                    e -> {
                                        e.setHealth(e.getHealth() - 5);
                                        e.setInvulnerableTicks(spawnTicks + 5);
                                    }, 
                                    e -> spawnTicks >= 215 || e.getHealth() <= 5)) {
                                CrazyDiamondHeal.handle(world, targetEntity, targetEntity, 
                                        w -> {
                                            BlockPos blockPos = w.blockPosition();
                                            world.setBlockAndUpdate(blockPos.offset(0, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
                                            world.setBlockAndUpdate(blockPos.offset(1, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
                                            world.setBlockAndUpdate(blockPos.offset(-1, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
                                            world.setBlockAndUpdate(blockPos, Blocks.SOUL_SAND.defaultBlockState());
                                            world.setBlockAndUpdate(blockPos.offset(0, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
                                            world.setBlockAndUpdate(blockPos.offset(1, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
                                            world.setBlockAndUpdate(blockPos.offset(-1, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
                                            w.remove();
                                        }, e -> true);
                            }
                        }
                    }
                }
            }
            break;
        case BLOCK:
            if (userPower.getResolveLevel() >= 2) {
                // FIXME !! (prev state) revert blocks (1 block to 1 block uncrafting)
                // FIXME !! (prev state) particles

            }
            break;
        default:
            // FIXME !! (prev state) revert items (uncrafting)
            // FIXME !! (prev state) particles
            if (!world.isClientSide() && userPower.getUser() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) userPower.getUser();
                ItemStack heldItem = player.getOffhandItem();
                if (!heldItem.isEmpty()) {
                    List<ICraftingRecipe> recipes = new ArrayList<>();
                    for (IRecipe<?> recipe : world.getRecipeManager().getRecipes()) {
                        if (recipe instanceof ICraftingRecipe && recipe.canCraftInDimensions(3, 3)
                                && !recipe.getIngredients().isEmpty() && matches(heldItem, recipe.getResultItem())) {
                            recipes.add((ICraftingRecipe) recipe);
                        }
                    }

                    if (!recipes.isEmpty()) {
                        ICraftingRecipe randomRecipe = recipes.get(player.getRandom().nextInt(recipes.size()));
                        ItemStack[] ingredients = getIngredients(randomRecipe);
                        boolean gaveIngredients = false;
                        for (ItemStack ingredient : ingredients) {
                            if (!ingredient.isEmpty()) {
                                JojoModUtil.giveItem(player, ingredient);
                                gaveIngredients = true;
                            }
                        }
                        if (gaveIngredients) {
                            heldItem.shrink(randomRecipe.getResultItem().getCount());
                        }
                    }
                }
            }
            break;
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
