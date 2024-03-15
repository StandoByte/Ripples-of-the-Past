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
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class CrazyDiamondPreviousState extends StandEntityAction {
    private final StandRelativeOffset userOffsetLeftArm;

    public CrazyDiamondPreviousState(StandEntityAction.Builder builder) {
        super(builder);
        this.userOffsetLeftArm = builder.userOffset.copyScale(-1, 1, 1);
    }

    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower standPower) {
        switch (target.getType()) {
        case BLOCK:
//            if (standPower.getResolveLevel() >= 2) {
//                BlockPos blockPos = target.getBlockPos();
//                BlockState blockState = user.level.getBlockState(blockPos);
//                ItemStack blockItem = new ItemStack(blockState.getBlock().asItem());
//                return ActionConditionResult.noMessage(convertTo(blockItem, user.level, recipe -> {
//                    ItemStack[] ingredients = getIngredients(recipe);
//                    return ingredients.length == 1 && !ingredients[0].isEmpty() && ingredients[0].getItem() instanceof BlockItem;
//                }, user.getRandom()).isPresent());
//            }
            return ActionConditionResult.NEGATIVE;
        case ENTITY:
            Entity targetEntity = target.getEntity();
            
            int resolveLevel = standPower.getResolveLevel();
            if (resolveLevel >= 3) {
                if (
                        targetEntity instanceof TNTEntity ||
                        targetEntity.getType() == EntityType.SNOW_GOLEM ||
                        targetEntity instanceof CreeperEntity && ((CreeperEntity) targetEntity).isPowered()) {
                    return ActionConditionResult.POSITIVE;
                }
                if (resolveLevel >= 4) {
                    if (
                            targetEntity.getType() == EntityType.IRON_GOLEM ||
                            targetEntity.getType() == EntityType.WITHER && ((WitherEntity) targetEntity).getInvulnerableTicks() > 0) {
                        return ActionConditionResult.POSITIVE;
                    }
                }
            }
            return conditionMessage("entity_revert");
        default:
            return ActionConditionResult.POSITIVE;
        }
    }
    
    @Override
    public ActionConditionResult checkStandTarget(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        if (target.getEntity() instanceof TNTEntity) {
            return ActionConditionResult.POSITIVE;
        }
        return super.checkStandTarget(target, standEntity, standPower);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (target.getType() == TargetType.EMPTY) {
            ItemStack heldItem = user.getOffhandItem();
            if (heldItem.isEmpty()) return conditionMessage("item_offhand");
            return convertTo(heldItem, user.level, null, user.getRandom(), false).isPresent() ? ActionConditionResult.POSITIVE : conditionMessage("item_revert");
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        ActionTarget target = task.getTarget();
        switch (target.getType()) {
        case ENTITY:
            if (userPower.getResolveLevel() >= 3) {
                Entity targetEntity = target.getEntity();
                boolean healTick = false;
                if (!targetEntity.isAlive()) {
                    return;
                }

                if (targetEntity instanceof TNTEntity) {
                    TNTEntity tnt = (TNTEntity) targetEntity;
                    if (!CrazyDiamondHeal.heal(world, tnt, tnt, (e, clientSide) -> {
                        if (!clientSide) {
                            e.setFuse(e.getLife() + 2);
                        }
                    }, e -> task.getTick() == 0 || e.getFuse() < 80)) {
                        CrazyDiamondHeal.heal(world, tnt, tnt, 
                                (e, clientSide) -> {
                                    if (!clientSide) {
                                        Block tntBlock = ForgeRegistries.BLOCKS.getValue(e.getType().getRegistryName());
                                        if (tntBlock == null || tntBlock.getRegistryName().equals(ForgeRegistries.BLOCKS.getDefaultKey())) {
                                            tntBlock = Blocks.TNT;
                                        }
                                        BlockPos blockPos = e.blockPosition();
                                        e.remove();
                                        if (!e.isAlive()) {
                                            replaceOrDropBlock(world, blockPos, tntBlock.defaultBlockState());
                                        }
                                    }
                                }, e -> true);
                    }
                    healTick = true;
                }

                else if (targetEntity.getType() == EntityType.SNOW_GOLEM) {
                    if (!CrazyDiamondHeal.healLivingEntity(world, (LivingEntity) targetEntity, standEntity)) {
                        CrazyDiamondHeal.heal(world, targetEntity, targetEntity, 
                                (e, clientSide) -> {
                                    if (!clientSide && standEntity.getRandom().nextFloat() < 0.1F) {
                                        BlockPos blockPos = e.blockPosition();
                                        e.remove();
                                        if (!e.isAlive()) {
                                            replaceOrDropBlock(world, blockPos.offset(0, 2, 0), Blocks.CARVED_PUMPKIN.defaultBlockState());
                                            replaceOrDropBlock(world, blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
                                            replaceOrDropBlock(world, blockPos.offset(0, 1, 0), Blocks.SNOW_BLOCK.defaultBlockState());
                                        }
                                    }
                                }, e -> true);
                    }
                    healTick = true;
                }
                
                else if (targetEntity instanceof CreeperEntity) {
                    CreeperEntity creeper = (CreeperEntity) targetEntity;
                    if (creeper.isPowered() && !CrazyDiamondHeal.healLivingEntity(world, (LivingEntity) targetEntity, standEntity)) {
                        CrazyDiamondHeal.heal(world, targetEntity, targetEntity, 
                                (e, clientSide) -> {
                                    if (!clientSide && standEntity.getRandom().nextFloat() < 0.05F) {
                                        creeper.getEntityData().set(CommonReflection.getCreeperPoweredParameter(), false);
                                    }
                                }, e -> true);
                    }
                    healTick = true;
                }

                else if (userPower.getResolveLevel() >= 4) {
                    if (targetEntity.getType() == EntityType.IRON_GOLEM) {
                        if (!CrazyDiamondHeal.healLivingEntity(world, (LivingEntity) targetEntity, standEntity)) {
                            CrazyDiamondHeal.heal(world, targetEntity, targetEntity, 
                                    (e, clientSide) -> {
                                        if (!clientSide && standEntity.getRandom().nextFloat() < 0.05F) {
                                            BlockPos blockPos = e.blockPosition();
                                            e.remove();
                                            if (!e.isAlive()) {
                                                replaceOrDropBlock(world, blockPos, Blocks.IRON_BLOCK.defaultBlockState());
                                                replaceOrDropBlock(world, blockPos.offset(0, 2, 0), Blocks.CARVED_PUMPKIN.defaultBlockState());
                                                replaceOrDropBlock(world, blockPos.offset(0, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
                                                replaceOrDropBlock(world, blockPos.offset(1, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
                                                replaceOrDropBlock(world, blockPos.offset(-1, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
                                            }
                                        }
                                    }, e -> true);
                        }
                        healTick = true;
                    }

                    else if (targetEntity.getType() == EntityType.WITHER) {
                        WitherEntity wither = (WitherEntity) targetEntity;
                        int spawnTicks = wither.getInvulnerableTicks();
                        if (spawnTicks > 0) {
                            if (!CrazyDiamondHeal.heal(world, wither, wither, 
                                    (e, clientSide) -> {
                                        if (!clientSide) {
                                            e.setInvulnerableTicks(Math.min(spawnTicks + 5, 220));
                                        }
                                    }, 
                                    e -> spawnTicks < 215)) {
                                CrazyDiamondHeal.heal(world, targetEntity, targetEntity, 
                                        (w, clientSide) -> {
                                            if (!clientSide && standEntity.getRandom().nextFloat() < 0.005F) {
                                                BlockPos blockPos = w.blockPosition();
                                                w.remove();
                                                if (!w.isAlive()) {
                                                    replaceOrDropBlock(world, blockPos.offset(0, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
                                                    replaceOrDropBlock(world, blockPos.offset(1, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
                                                    replaceOrDropBlock(world, blockPos.offset(-1, 2, 0), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
                                                    replaceOrDropBlock(world, blockPos, Blocks.SOUL_SAND.defaultBlockState());
                                                    replaceOrDropBlock(world, blockPos.offset(0, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
                                                    replaceOrDropBlock(world, blockPos.offset(1, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
                                                    replaceOrDropBlock(world, blockPos.offset(-1, 1, 0), Blocks.SOUL_SAND.defaultBlockState());
                                                }
                                            }
                                        }, e -> true);
                            }
                        }
                        healTick = true;
                    }
                }
                
                if (!world.isClientSide()) {
                    barrageVisualsTick(standEntity, healTick, targetEntity != null ? targetEntity.getBoundingBox().getCenter() : null);
                }
            }
            break;
//        case BLOCK:
//            BlockPos blockPos = target.getBlockPos();
//            if (!world.isClientSide()) {
//                BlockState blockState = world.getBlockState(blockPos);
//                ItemStack blockItem = new ItemStack(blockState.getBlock().asItem());
//                convertTo(blockItem, world, recipe -> {
//                    ItemStack[] ingredients = getIngredients(recipe);
//                    return ingredients.length == 1 && !ingredients[0].isEmpty() && ingredients[0].getItem() instanceof BlockItem
//                            && ingredients[0].getCount() == recipe.getResultItem().getCount();
//                }, standEntity.getRandom()).ifPresent(oneItemArray -> {
//                    BlockItem item = (BlockItem) oneItemArray.getLeft()[0].getItem();
//                    world.setBlockAndUpdate(blockPos, item.getBlock().defaultBlockState());
//                });
//            }
//            else {
//                CrazyDiamondRestoreTerrain.addParticlesAroundBlock(world, blockPos, standEntity.getRandom());
//            }
//            break;
        default:
            if (!world.isClientSide()) {
                ItemStack heldItem = userPower.getUser().getOffhandItem();
                if (ModStandsInit.CRAZY_DIAMOND_REPAIR.get().repairTick(userPower.getUser(), standEntity, heldItem, task.getTick()) == 0
                        && userPower.getUser() instanceof PlayerEntity && CrazyDiamondRepairItem.itemTransformationTick(task.getTick(), standEntity)) {
                    PlayerEntity player = (PlayerEntity) userPower.getUser();
                    CrazyDiamondRepairItem.dropExperience(player, heldItem);
                    convertTo(heldItem, world, null, standEntity.getRandom(), true).ifPresent(itemsAndCount -> {
                        boolean gaveIngredients = false;
                        for (ItemStack ingredient : itemsAndCount.getLeft()) {
                            if (!ingredient.isEmpty()) {
                                MCUtil.giveItemTo(player, ingredient, true);
                                gaveIngredients = true;
                            }
                        }
                        if (gaveIngredients) {
                            heldItem.shrink(itemsAndCount.getRight());
                        }
                    });
                }
            }
            else if (ClientUtil.canSeeStands()) {
                CustomParticlesHelper.createCDRestorationParticle(userPower.getUser(), Hand.OFF_HAND);
            }
            break;
        }
    }

    private static final Optional<Pair<ItemStack[], Integer>> EXISTS = Optional.of(Pair.of(new ItemStack[0], 0));
    private Optional<Pair<ItemStack[], Integer>> convertTo(ItemStack item, World world, 
            @Nullable Predicate<IRecipe<?>> additionalCondition, Random random, boolean createItems) {
        if (item.isEmpty()) return Optional.empty();
        
        if (item.getItem() == Items.ENCHANTED_BOOK)     return createItems ? Optional.of(Pair.of(new ItemStack[]{new ItemStack(Items.BOOK)}, 1)) : EXISTS;
        // FIXME free up the map id
//        if (item.getItem() == Items.FILLED_MAP)         return createItems ? Optional.of(Pair.of(new ItemStack[]{new ItemStack(Items.MAP)}, 1))  : EXISTS;
        
        if (item.getItem() == Items.WRITTEN_BOOK) {
            if (createItems) {
                ItemStack writableBook = new ItemStack(Items.WRITABLE_BOOK);
                writableBook.setTag(revertBookPagesNBT(item.getTag()));
                return Optional.of(Pair.of(new ItemStack[]{writableBook}, 1));
            } else {
                return EXISTS;
            }
        }

        // FIXME revert brewing recipes
        return GeneralUtil.groupByPredicatesOrdered(
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
                    if (ingredients.length == 0) return Optional.empty();
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
    
    private CompoundNBT revertBookPagesNBT(CompoundNBT signedBookNBT) {
        CompoundNBT nbt = new CompoundNBT();
        if (signedBookNBT.contains("pages", MCUtil.getNbtId(ListNBT.class))) {
            ListNBT textPagesClean = new ListNBT();
            
            signedBookNBT.getList("pages", MCUtil.getNbtId(StringNBT.class)).forEach(pageNBT -> {
                if (pageNBT.getId() == MCUtil.getNbtId(StringNBT.class)) {
                    ITextComponent text = ITextComponent.Serializer.fromJson(((StringNBT) pageNBT).getAsString());
                    if (text != null) {
                        textPagesClean.add(StringNBT.valueOf(text.getString()));
                    }
                }
            });
            
            nbt.put("pages", textPagesClean);
        }
        return nbt;
    }
    
    public static boolean canReplaceBlock(World world, BlockPos blockPos, BlockState newBlockState) {
        BlockState currentBlockState = world.getBlockState(blockPos);
        float hardness = currentBlockState.getDestroySpeed(world, blockPos);
        return currentBlockState.getMaterial().isReplaceable() || hardness >= 0 && hardness < newBlockState.getDestroySpeed(world, blockPos);
    }
    
    private void replaceOrDropBlock(World world, BlockPos blockPos, BlockState newBlockState) {
        if (!world.isClientSide()) {
            if (canReplaceBlock(world, blockPos, newBlockState)) {
                world.destroyBlock(blockPos, true);
                world.setBlockAndUpdate(blockPos, newBlockState);
            }
            else {
                Item blockItem = newBlockState.getBlock().asItem();
                if (blockItem != null && blockItem != Items.AIR) {
                    ItemStack dropAsItem = new ItemStack(blockItem);
                    Vector3d pos = Vector3d.atCenterOf(blockPos);
                    ItemEntity itemEntity = new ItemEntity(world, pos.x, pos.y, pos.z, dropAsItem);
                    world.addFreshEntity(itemEntity);
                }
            }
        }
    }
    
    @Override
    public void phaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {
        if (world.isClientSide()) {
            if (to == Phase.PERFORM) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
                        ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, Phase.PERFORM, 1.0F, 1.0F, true);
            }
            else if (from == Phase.PERFORM) {
                standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    @Override
    protected boolean barrageVisuals(StandEntity standEntity, IStandPower standPower, StandEntityTask task) {
        return super.barrageVisuals(standEntity, standPower, task)
                && task.getTarget().getType() == TargetType.ENTITY && checkTarget(task.getTarget(), standPower.getUser(), standPower).isPositive();
    }

    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return offsetToTarget(standPower, standEntity, task, 0, standEntity.getMaxEffectiveRange(), null)
                .orElse(!standEntity.isArmsOnlyMode() && standEntity.getUser().getMainArm() == HandSide.LEFT ? 
                        userOffsetLeftArm
                        : super.getOffsetFromUser(standPower, standEntity, task));
    }

    @Override
    public float yRotForOffset(LivingEntity user, StandEntityTask task) {
        return task.getTarget().getType() != TargetType.EMPTY ? super.yRotForOffset(user, task) : user.yBodyRot;
    }
    
    @Override
    public void rotateStand(StandEntity standEntity, StandEntityTask task) {
        if (standEntity.isArmsOnlyMode() || task.getTarget().getType() != TargetType.EMPTY) {
            super.rotateStand(standEntity, task);
        }
        else if (!standEntity.isRemotePositionFixed()) {
            LivingEntity user = standEntity.getUser();
            if (user != null) {
                float rotationOffset = user.getMainArm() == HandSide.RIGHT ? 15 : -15;
                standEntity.setRot(user.yBodyRot + rotationOffset, user.xRot);
                standEntity.setYHeadRot(user.yBodyRot + rotationOffset);
            }
        }
    }
}
