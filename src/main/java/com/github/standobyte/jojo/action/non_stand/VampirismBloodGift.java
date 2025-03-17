package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.util.mc.MCUtil;

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
        Optional<INonStandPower> powerData = INonStandPower.getNonStandPowerOptional(targetLiving).resolve();
        if (!powerData.isPresent()) {
            return conditionMessage("cant_become_vampire");
        }
        Optional<NonStandPowerType<?>> curType = powerData.map(targetPower -> targetPower.getType());
        if (curType.isPresent()) {
            if (curType.get() == ModPowers.VAMPIRISM.get()) {
                return conditionMessage("already_vampire");
            }
            else {
                return conditionMessage("cant_become_vampire");
            }
        }
        if (targetLiving.getHealth() > 6.0F) {
            return conditionMessage("target_too_many_health");
        }
        return super.checkTarget(target, user, power);
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
        Entity targetEntity = target.getEntity();
        if (targetEntity instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) targetEntity;

            if (!world.isClientSide()) {
                if (INonStandPower.getNonStandPowerOptional(targetLiving).map(
                        targetPower -> targetPower.givePower(ModPowers.VAMPIRISM.get())).orElse(false)) {
                    user.hurt(new DamageSource("blood_gift").bypassArmor(), 10.0F);
                    boolean wasDead = targetLiving.getHealth() <= 0;
                    targetLiving.heal(targetLiving.getMaxHealth());
                    if (wasDead) {
                        MCUtil.onLivingResurrect(targetLiving);
                    }
                }

                targetLiving.deathTime = 0;
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
