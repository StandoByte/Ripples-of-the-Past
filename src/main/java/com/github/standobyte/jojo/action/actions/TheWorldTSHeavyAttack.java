package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class TheWorldTSHeavyAttack extends StandEntityAction {
    public static final StandPose TS_PUNCH_POSE = new StandPose("TS_PUNCH");
    private final Supplier<StandEntityHeavyAttack> theWorldHeavyAttack;
    private final Supplier<TimeStopInstant> theWorldTimeStopBlink;

    public TheWorldTSHeavyAttack(StandEntityAction.Builder builder, 
            Supplier<StandEntityHeavyAttack> theWorldHeavyAttack, Supplier<TimeStopInstant> theWorldTimeStopBlink) {
        super(builder);
        this.theWorldHeavyAttack = theWorldHeavyAttack;
        this.theWorldTimeStopBlink = theWorldTimeStopBlink;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (TimeUtil.isTimeStopped(user.level, user.blockPosition())) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkSpecificConditions(user, power, target);
    }

    // FIXME (!!) (TW ts punch) no consecutive uses
    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() || !stand.isFollowingUser()
        		? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }

    // FIXME (!!) (TW ts punch) doesn't always work in manual control mode
    @Override
    public ActionTarget targetBeforePerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            return ActionTarget.fromRayTraceResult(stand.aimWithStandOrUser(stand.getMaxRange(), target));
        }
        return super.targetBeforePerform(world, user, power, target);
    }
    
    @Override
    protected void onTaskInit(IStandPower standPower, StandEntity standEntity, ActionTarget target) {
        LivingEntity user = standPower.getUser();
        if (user != null) {
            Vector3d pos = target.getTargetPos(false);
            if (pos != null) {
                double offset = 0.5 + standEntity.getBbWidth();
                if (target.getType() == TargetType.ENTITY) {
                    offset += target.getEntity(standEntity.level).getBoundingBox().getXsize() / 2;
                }
                pos = pos.subtract(pos.subtract(user.getEyePosition(1.0F)).normalize().scale(offset));
            }
            else {
                pos = user.position().add(standEntity.getLookAngle().scale(standEntity.getMaxRange()));
            }
            // FIXME (!!) (TW ts punch) TW pos desync
            // FIXME (!!) (TW ts punch) the attack is weak due to small effective range
            // FIXME (!!) (TW ts punch) use stamina
            standEntity.teleportTo(pos.x, pos.y, pos.z);
        }
        standEntity.updateStrengthMultipliers(standEntity.getUser());
    }

    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return 5;
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackRecovery(standEntity.getAttackSpeed());
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.punch(PunchType.HEAVY_NO_COMBO, target, this, 1, attack -> {
                attack
                .armorPiercing(0)
                .addKnockback(4)
                .callbackAfterAttack((t, stand, power, user, hurt, killed) -> {
                    if (killed) {
                        JojoModUtil.sayVoiceLine(user, ModSounds.DIO_THIS_IS_THE_WORLD.get());
                    }
                });
            });
        }
    }
    
    @Override
    public boolean noAdheringToUserOffset(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    @Override
    public boolean isCombatAction() {
        return true;
    }
    
    @Override
    public boolean canFollowUpBarrage() {
        return true;
    }
}
