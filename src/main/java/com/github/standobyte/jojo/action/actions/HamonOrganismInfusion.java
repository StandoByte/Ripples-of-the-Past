package com.github.standobyte.jojo.action.actions;

import java.util.Set;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonOrganismInfusion extends HamonAction {

    public HamonOrganismInfusion(HamonAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        ItemStack heldItemStack = user.getMainHandItem();
        if (!heldItemStack.isEmpty()) {
            return conditionMessage("hand");
        }
        switch (target.getType()) {
        case ENTITY:
            Entity entity = target.getEntity(user.level);
            if (!(entity instanceof AnimalEntity || entity instanceof AmbientEntity)) {
                return conditionMessage("animal");
            }
            if (!power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get().isSkillLearned(HamonSkill.ANIMAL_INFUSION)) {
                return conditionMessage("animal_infusion");
            }
            break;
        case BLOCK:
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = user.level.getBlockState(blockPos);
            if (blockState.getMaterial() == Material.EGG && 
                    !power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get().isSkillLearned(HamonSkill.ANIMAL_INFUSION)) {
                return conditionMessage("animal_infusion");
            }
            Block block = blockState.getBlock();
            if (!(isBlockLiving(blockState) || block instanceof FlowerPotBlock && blockState.getBlock() != Blocks.FLOWER_POT)) {
                return conditionMessage("living_plant");
            }
            break;
        default:
            break;
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
            int chargeTicks = 100 + MathHelper.floor((float) (1100 * hamon.getHamonStrengthLevel()) / (float) HamonData.MAX_STAT_LEVEL);
            switch(target.getType()) {
            case BLOCK:
                HamonBlockChargeEntity charge = new HamonBlockChargeEntity(world, target.getBlockPos());
                charge.setCharge(0.02F * hamon.getHamonDamageMultiplier(), chargeTicks, user, getEnergyCost(power));
                world.addFreshEntity(charge);
                break;
            case ENTITY:
                LivingEntity entity = (LivingEntity) target.getEntity(world);
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> 
                cap.setHamonCharge(0.2F * hamon.getHamonDamageMultiplier(), chargeTicks, user, getEnergyCost(power)));
                break;
            default:
                break;
            }
        }
    }

    private static final Set<Material> LIVING_MATERIALS = ImmutableSet.<Material>builder().add(
            Material.PLANT,
            Material.WATER_PLANT,
            Material.REPLACEABLE_PLANT,
            Material.REPLACEABLE_FIREPROOF_PLANT,
            Material.REPLACEABLE_WATER_PLANT,
            Material.GRASS,
            Material.BAMBOO_SAPLING,
            Material.BAMBOO,
            Material.LEAVES,
            Material.CACTUS,
            Material.CORAL,
            Material.VEGETABLE,
            Material.EGG
            ).build();
    public static boolean isBlockLiving(BlockState blockState) {
        return LIVING_MATERIALS.contains(blockState.getMaterial());
    }

}
