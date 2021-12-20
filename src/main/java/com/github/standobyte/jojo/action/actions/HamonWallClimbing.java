package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonWallClimbing extends HamonAction {

    public HamonWallClimbing(Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, INonStandPower power, ActionTarget target) {
        ItemStack heldItemStack = performer.getMainHandItem();
        if (!user.horizontalCollision) {
            if (user.level.isClientSide()) {
                if (user instanceof PlayerEntity) {
                    return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
                }
            }
            else if (!(user instanceof PlayerEntity)) {
                return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
            }
        }
        if (!heldItemStack.isEmpty()) {
            return conditionMessage("hand");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void onHoldTickUser(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
            Vector3d movement = user.getDeltaMovement();
            user.setDeltaMovement(movement.x, 0.1D + hamon.getBreathingLevel() * 0.0015 + hamon.getHamonControlLevel() * 0.0025, movement.z);
            hamon.hamonPointsFromAction(HamonStat.CONTROL, getHeldTickManaCost());
        }
        if (ticksHeld % 4 == 0) {
            Vector3d sparkVec = user.getLookAngle().scale(0.25).add(user.getX(), user.getY(1.0), user.getZ());
            HamonPowerType.createHamonSparkParticles(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, sparkVec, 0.1F);
        }
    }

}
