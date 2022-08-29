package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class CrazyDiamondPreviousState extends StandEntityAction {

    public CrazyDiamondPreviousState(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower standPower) {
        switch (target.getType()) {
        case BLOCK:
            if (standPower.getResolveLevel() >= 2) {
                BlockPos blockPos = target.getBlockPos();
                BlockState blockState = user.level.getBlockState(blockPos);
                ItemStack blockItem = new ItemStack(blockState.getBlock().asItem());
                return ActionConditionResult.noMessage(convertTo(blockItem, user.level, recipe -> {
                    ItemStack[] ingredients = getIngredients(recipe);
                    return ingredients.length == 1 && !ingredients[0].isEmpty() && ingredients[0].getItem() instanceof BlockItem;
                }, user.getRandom()).isPresent());
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
            return ActionConditionResult.POSITIVE;
        }
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (target.getType() == TargetType.EMPTY) {
            ItemStack heldItem = user.getOffhandItem();
            if (heldItem.isEmpty()) return conditionMessage("item_offhand");
            return convertTo(heldItem, user.level, null, user.getRandom()).isPresent() ? ActionConditionResult.POSITIVE : conditionMessage("item_revert");
        }
        return super.checkSpecificConditions(user, power, target);
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
                    if (CrazyDiamondHeal.handle(world, tnt, tnt, (e, clientSide) -> {
                        if (!clientSide) {
                            e.setFuse(task.getTick() == 0 ? e.getFuse() - e.tickCount + 2 : e.getFuse() + 2);
                        }
                    }, e -> e.getFuse() < 80)) {
                        CrazyDiamondHeal.handle(world, tnt, tnt, 
                                (e, clientSide) -> {
                                    if (!clientSide) {
                                        Block tntBlock = ForgeRegistries.BLOCKS.getValue(e.getType().getRegistryName());
                                        if (tntBlock == null || tntBlock.getRegistryName().equals(ForgeRegistries.BLOCKS.getDefaultKey())) {
                                            tntBlock = Blocks.TNT;
                                        }
                                        BlockPos blockPos = e.blockPosition();
                                        e.remove();
//                                        CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos, tntBlock.defaultBlockState());
                                    }
                                }, e -> true);
                    }
                    return;
                }

                else if (targetEntity.getType() == EntityType.SNOW_GOLEM) {
                    if (CrazyDiamondHeal.handleLivingEntity(world, (LivingEntity) targetEntity)) {
                        CrazyDiamondHeal.handle(world, targetEntity, targetEntity, 
                                (e, clientSide) -> {
                                    if (!clientSide && standEntity.getRandom().nextFloat() < 0.1F) {
                                        BlockPos blockPos = e.blockPosition();
//                                        CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(0, 2, 0), Blocks.CARVED_PUMPKIN.defaultBlockState());
//                                        CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
//                                        CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(0, 1, 0), Blocks.SNOW_BLOCK.defaultBlockState());
                                        e.remove();
                                    }
                                }, e -> true);
                    }
                    return;
                }

                else if (userPower.getResolveLevel() >= 4) {
                    if (targetEntity.getType() == EntityType.IRON_GOLEM) {
                        if (CrazyDiamondHeal.handleLivingEntity(world, (LivingEntity) targetEntity)) {
                            CrazyDiamondHeal.handle(world, targetEntity, targetEntity, 
                                    (e, clientSide) -> {
                                        if (!clientSide && standEntity.getRandom().nextFloat() < 0.05F) {
                                            BlockPos blockPos = e.blockPosition();
//                                            CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos, Blocks.IRON_BLOCK.defaultBlockState());
//                                            CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(0, 2, 0), Blocks.CARVED_PUMPKIN.defaultBlockState());
//                                            CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(0, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
//                                            CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(1, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
//                                            CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(-1, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
                                            e.remove();
                                        }
                                    }, e -> true);
                        }
                        return;
                    }

                    else if (targetEntity.getType() == EntityType.WITHER) {
                        WitherEntity wither = (WitherEntity) targetEntity;
                        int spawnTicks = wither.getInvulnerableTicks();
                        if (spawnTicks > 0) {
                            if (CrazyDiamondHeal.handle(world, wither, wither, 
                                    (e, clientSide) -> {
                                        if (!clientSide) {
                                            e.setHealth(e.getHealth() - 5);
                                            e.setInvulnerableTicks(spawnTicks + 5);
                                        }
                                    }, 
                                    e -> spawnTicks >= 215 || e.getHealth() <= 5)) {
                                CrazyDiamondHeal.handle(world, targetEntity, targetEntity, 
                                        (w, clientSide) -> {
                                            if (!clientSide) {
                                                BlockPos blockPos = w.blockPosition();
//                                                CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(0, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
//                                                CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(1, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
//                                                CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(-1, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
//                                                CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos, Blocks.SOUL_SAND.defaultBlockState());
//                                                CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(0, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
//                                                CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(1, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
//                                                CrazyDiamondRestoreTerrain.replaceBlock(world, blockPos.offset(-1, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
                                                w.remove();
                                            }
                                        }, e -> true);
                            }
                        }
                    }
                }
            }
            break;
        case BLOCK:
            BlockPos blockPos = target.getBlockPos();
            if (!world.isClientSide()) {
                BlockState blockState = world.getBlockState(blockPos);
                ItemStack blockItem = new ItemStack(blockState.getBlock().asItem());
                convertTo(blockItem, world, recipe -> {
                    ItemStack[] ingredients = getIngredients(recipe);
                    return ingredients.length == 1 && !ingredients[0].isEmpty() && ingredients[0].getItem() instanceof BlockItem
                            && ingredients[0].getCount() == recipe.getResultItem().getCount();
                }, standEntity.getRandom()).ifPresent(oneItemArray -> {
                    BlockItem item = (BlockItem) oneItemArray.getLeft()[0].getItem();
                    world.setBlockAndUpdate(blockPos, item.getBlock().defaultBlockState());
                });
            }
            else {
                CrazyDiamondRestoreTerrain.addParticlesAroundBlock(world, blockPos, standEntity.getRandom());
            }
            break;
        default:
            if (!world.isClientSide()) {
                ItemStack heldItem = userPower.getUser().getOffhandItem();
                if (ModActions.CRAZY_DIAMOND_REPAIR.get().repairTick(userPower.getUser(), heldItem, task.getTick()) == 0
                        && userPower.getUser() instanceof PlayerEntity && task.getTick() % 10 == 9) {
                    PlayerEntity player = (PlayerEntity) userPower.getUser();
                    convertTo(heldItem, world, null, standEntity.getRandom()).ifPresent(itemsAndCount -> {
                        boolean gaveIngredients = false;
                        for (ItemStack ingredient : itemsAndCount.getLeft()) {
                            if (!ingredient.isEmpty()) {
                                JojoModUtil.giveItemToPlayer(player, ingredient);
                                gaveIngredients = true;
                            }
                        }
                        if (gaveIngredients) {
                            heldItem.shrink(itemsAndCount.getRight());
                        }
                    });
                }
            }
            else {
                CustomParticlesHelper.createCDRestorationParticle(userPower.getUser(), Hand.OFF_HAND);
            }
            break;
        }
    }
    
    private Optional<Pair<ItemStack[], Integer>> convertTo(ItemStack item, World world, 
            @Nullable Predicate<IRecipe<?>> additionalCondition, Random random) {
        if (item.isEmpty()) return Optional.empty();

        // FIXME revert brewing recipes
        return JojoModUtil.groupByPredicatesOrdered(
                world.getRecipeManager().getRecipes().stream(), Util.make(new ArrayList<>(), list -> {
                    // FIXME revert nbt recipes (including netherite armor)
                    list.add(recipe -> recipe instanceof SmithingRecipe);
                    list.add(recipe -> recipe instanceof AbstractCookingRecipe);
                    list.add(recipe -> recipe instanceof StonecuttingRecipe);
                    list.add(recipe -> recipe instanceof ICraftingRecipe);
                    list.add(recipe -> true);
                }), recipe -> outputMatches(recipe, item) && !bannedItem(item, world) && (additionalCondition == null || additionalCondition.test(recipe)), false)
        .values().stream().filter(list -> !list.isEmpty()).findFirst()
        .flatMap(recipesOfPreferredType -> {
            IRecipe<?> randomRecipe = recipesOfPreferredType.get(random.nextInt(recipesOfPreferredType.size()));
            ItemStack[] ingredients = getIngredients(randomRecipe);
            return Optional.of(Pair.of(ingredients, randomRecipe.getResultItem().getCount()));
        });
    }
    
    private boolean outputMatches(IRecipe<?> recipe, ItemStack stack) {
        return recipe.getResultItem().getItem() == stack.getItem() && recipe.getResultItem().getCount() <= stack.getCount();
    }
    
    private boolean bannedItem(ItemStack stack, World world) {
        return world.getRecipeManager().getRecipes().stream().anyMatch(recipe -> 
        recipe.getResultItem().getItem() == stack.getItem() && recipe instanceof BlastingRecipe);
    }

    private ItemStack[] getIngredients(IRecipe<?> recipe) {
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
