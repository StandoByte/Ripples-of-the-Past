package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismBloodGift extends VampirismAction {

    public VampirismBloodGift(NonStandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        Entity targetEntity = target.getEntity();
        if (!(targetEntity instanceof PlayerEntity)) {
            return conditionMessage("player_target");
        }
        LivingEntity targetLiving = (LivingEntity) targetEntity;
        if (INonStandPower.getNonStandPowerOptional(targetLiving).map(targetPower -> targetPower.hasPower()).orElse(true)) {
            return conditionMessage("cant_become_vampire");
        }
        if (targetLiving.getHealth() > 6.0F) {
            return conditionMessage("target_too_many_health");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        if (!user.getMainHandItem().isEmpty()) {
            return conditionMessage("hand");
        }
        if (user.getHealth() <= 10.0F) {
            return conditionMessage("user_too_low_health");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            PlayerEntity targetPlayer = (PlayerEntity) target.getEntity();
            if (INonStandPower.getNonStandPowerOptional(targetPlayer).map(
                    targetPower -> targetPower.givePower(ModPowers.VAMPIRISM.get())).orElse(false)) {
                user.hurt(new DamageSource("blood_gift").bypassArmor(), 10.0F);
                targetPlayer.heal(targetPlayer.getMaxHealth());
            }
        }
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public double getMaxRangeSqEntityTarget() {
        return 4;
    }
    
    @Override
    protected int maxCuringStage() {
        return 3;
    }
}
