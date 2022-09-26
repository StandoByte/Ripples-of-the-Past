package com.github.standobyte.jojo.action.stand;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StandEntityLightAttack extends StandEntityAction implements IHasStandPunch {
    private final Supplier<SoundEvent> punchSound;

    public StandEntityLightAttack(StandEntityLightAttack.Builder builder) {
        super(builder);
        this.punchSound = builder.punchSound;
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        if (standEntity.isArmsOnlyMode() && standEntity.swingingArm == Hand.OFF_HAND) {
            standEntity.setArmsOnlyMode(true, true);
        }
        standEntity.alternateHands();
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        standEntity.punch(task, this, task.getTarget());
    }
    
    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return IHasStandPunch.super.punchEntity(stand, target, dmgSource)
                .damage(StandStatFormulas.getLightAttackDamage(stand.getAttackDamage()))
                .addKnockback(stand.guardCounter())
                .addCombo(0.15F)
                .parryTiming(stand.getComboMeter() == 0 ? StandStatFormulas.getParryTiming(stand.getPrecision()) : 0)
                .impactSound(punchSound);
    }
    
    @Override
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state) {
        return IHasStandPunch.super.punchBlock(stand, pos, state)
                .impactSound(punchSound);
    }
    
    @Override
    public StandMissedPunch punchMissed(StandEntity stand) {
        return IHasStandPunch.super.punchMissed(stand).swingSound(punchSound);
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackWindup(speed, standEntity.getComboMeter(), standEntity.guardCounter());
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getComboMeter());
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getComboMeter())
                * (standEntity.isArmsOnlyMode() ? 2 : 4);
    }
    
    @Override
    public List<Supplier<SoundEvent>> getSounds(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        return task.getTarget().getType() != TargetType.ENTITY || standEntity.isArmsOnlyMode() || standEntity.getComboMeter() > 0
        		? null : super.getSounds(standEntity, standPower, phase, task);
    }
    
    @Override
	protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
        return phase == Phase.RECOVERY || super.isCancelable(standPower, standEntity, newAction, phase);
    }
    
    @Override
    protected boolean isChainable(IStandPower standPower, StandEntity standEntity) {
    	return true;
    }
    
    @Override
    public boolean isFreeRecovery(IStandPower standPower, StandEntity standEntity) {
    	return true;
    }
    
    @Override
    public boolean noComboDecay() {
        return true;
    }
    
    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return true;
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityLightAttack.Builder>  {
        private Supplier<SoundEvent> punchSound = () -> null;
    	
    	public Builder() {
            staminaCost(10F).standUserSlowDownFactor(1.0F)
            .standOffsetFront().standOffsetFromUser(-0.75, 0.75)
            .standPose(StandPose.LIGHT_ATTACK).punchSound(ModSounds.STAND_PUNCH_LIGHT)
            .standAutoSummonMode(AutoSummonMode.MAIN_ARM)
            .partsRequired(StandPart.ARMS);
    	}
    	
    	public Builder punchSound(Supplier<SoundEvent> punchSound) {
    	    this.punchSound = punchSound != null ? punchSound : () -> null;
    	    return getThis();
    	}
    	
		@Override
		protected Builder getThis() {
			return this;
		}
    }
}
