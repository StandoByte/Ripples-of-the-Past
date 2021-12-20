package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonHealing extends HamonAction {

    public HamonHealing(Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, INonStandPower power, ActionTarget target) {
        if (!performer.getMainHandItem().isEmpty()) {
            return conditionMessage("hand");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
        float effectStr = (float) hamon.getHamonControlLevel() / (float) HamonData.MAX_STAT_LEVEL;
        if (!world.isClientSide()) {
            Entity targetEntity = target.getType() == TargetType.ENTITY && hamon.isSkillLearned(HamonSkill.HEALING_TOUCH) ? target.getEntity(world) : null;
            LivingEntity targetLiving = targetEntity instanceof LivingEntity ? (LivingEntity) targetEntity : null;
            LivingEntity entityToHeal = targetEntity != null && !JojoModUtil.isUndead(targetLiving) ? targetLiving : user;
            int regenDuration = 80 + MathHelper.floor(220F * effectStr);
            int regenLvl = MathHelper.floor(3.5F * effectStr);
//            if (entityToHeal.getHealth() < entityToHeal.getMaxHealth()) {
                hamon.hamonPointsFromAction(HamonStat.CONTROL, getManaCost());
//            }
            entityToHeal.addEffect(new EffectInstance(Effects.REGENERATION, regenDuration, regenLvl));
            if (hamon.isSkillLearned(HamonSkill.EXPEL_VENOM)) {
                entityToHeal.removeEffect(Effects.POISON);
                entityToHeal.removeEffect(Effects.WITHER);
                entityToHeal.removeEffect(Effects.HUNGER);
                entityToHeal.removeEffect(Effects.CONFUSION);
            }
            if (hamon.isSkillLearned(HamonSkill.PLANTS_GROWTH) && user instanceof PlayerEntity) {
                BlockPos pos = target.getType() == TargetType.BLOCK ? target.getBlockPos() : user.isOnGround() ? user.blockPosition().below() : null;
                if (pos != null) {
                    Direction face = target.getType() == TargetType.BLOCK ? target.getFace() : Direction.UP;
                    bonemealEffect(user.level, (PlayerEntity) user, pos, face);
                }
            }
        }
        Vector3d pos = target.getTargetPos();
        if (pos == null) {
            pos = new Vector3d(user.getX(), user.getY(0.5), user.getZ());
        }
        HamonPowerType.createHamonSparkParticles(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, 
                pos, Math.max(0.5F * effectStr, 0.1F));
    }

    public static boolean bonemealEffect(World world, PlayerEntity applyingPlayer, BlockPos pos, Direction face) {
        if (BoneMealItem.applyBonemeal(ItemStack.EMPTY, world, pos, applyingPlayer)) {
            if (!world.isClientSide()) {
                world.levelEvent(2005, pos, 0);
            }
            return true;
        } else {
            BlockPos posOffset = pos.relative(face);
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isFaceSturdy(world, pos, face) && BoneMealItem.growWaterPlant(new ItemStack(null), world, posOffset, face)) {
                if (!world.isClientSide()) {
                    world.levelEvent(2005, posOffset, 0);
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
