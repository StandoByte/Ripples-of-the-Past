package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityPosPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.MathHelper;
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

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() || stand.isBeingRetracted()
        		? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    protected boolean canBeQueued(IStandPower standPower, StandEntity standEntity) {
        return false;
    }

    @Override
    public ActionTarget targetBeforePerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
        	StandEntity stand = (StandEntity) power.getStandManifestation();
        	return ActionTarget.fromRayTraceResult(
        			JojoModUtil.rayTrace(stand.isManuallyControlled() ? stand : user, 
        					stand.getMaxRange(), stand.canTarget(), stand.getPrecision() / 16F, stand.getPrecision()));
        }
        return super.targetBeforePerform(world, user, power, target);
    }
    
    @Override
    protected void preTaskInit(World world, IStandPower standPower, StandEntity standEntity, ActionTarget target) {
    	if (!world.isClientSide() || standEntity.isManuallyControlled()) {
	    	LivingEntity aimingEntity = standEntity.isManuallyControlled() ? standEntity : standPower.getUser();
	    	if (aimingEntity != null) {
    			TimeStopInstant blink = theWorldTimeStopBlink.get();
    			TimeStop timeStop = blink.getBaseTimeStop();

	            int timeStopTicks = TimeStop.getTimeStopTicks(standPower, timeStop);
	            if (!StandUtil.standIgnoresStaminaDebuff(standPower) && blink != null) {
	                timeStopTicks = MathHelper.clamp(MathHelper.floor(
	                		(standPower.getStamina() - blink.getStaminaCost(standPower)) / blink.getStaminaCostTicking(standPower)
	                		), 0, timeStopTicks);
	            }

	            int ticksForWindup = 10;
	            if (standEntity.getCurrentTask().isPresent()) {
	            	ticksForWindup += 20;
	            }
	    		if (standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0) {
	    			Vector3d pos = target.getTargetPos(true);
	    			if (pos != null) {
	    				double offset = 0.5 + standEntity.getBbWidth();
	    				if (target.getType() == TargetType.ENTITY) {
	    					offset += target.getEntity().getBoundingBox().getXsize() / 2;
	    				}
	    				pos = pos.subtract(pos.subtract(aimingEntity.getEyePosition(1.0F)).normalize().scale(offset)).subtract(0, standEntity.getEyeHeight(), 0);
	    			}
	    			else {
	    				pos = aimingEntity.position().add(standEntity.getLookAngle().scale(standEntity.getMaxRange()));
	    			}
	    			

		            double ticksForDistance = pos.subtract(standEntity.position()).length() / TimeStopInstant.getDistancePerTick(standEntity);
		            
		            if (timeStopTicks < ticksForDistance + ticksForWindup) {
		            	pos = timeStopTicks > ticksForWindup ? pos.subtract(standEntity.position()).scale((double) timeStopTicks - ticksForWindup / ticksForDistance).add(standEntity.position()) : standEntity.position();
		            }
		            else {
		            	timeStopTicks = MathHelper.ceil(ticksForDistance) + ticksForWindup;
		            }
		    		
		            pos = standEntity.collideNextPos(pos);
//		    		if (!world.isClientSide() ^ standEntity.isManuallyControlled()) {
		    			standEntity.moveTo(pos);
		    			if (standEntity.tickCount == 0 && !world.isClientSide()) {
		    				PacketManager.sendToClientsTracking(new TrDirectEntityPosPacket(standEntity.getId(), pos), standEntity);
		    			}
//		    		}
	    		}
	    		else {
	    			timeStopTicks = ticksForWindup;
	    		}

	    		TimeStopInstant.skipTicksForStandAndUser(standPower, timeStopTicks);
	    		if (!world.isClientSide()) {
    				blink.playSound(world, standEntity);
	    			standPower.consumeStamina(blink.getStaminaCost(standPower) * 0.5F + timeStopTicks * blink.getStaminaCostTicking(standPower) * 2F);
	    			if (standPower.hasPower()) {
		    			StandStats stats = standPower.getType().getStats();
		    			if (stats instanceof TimeStopperStandStats) {
		    				standPower.addLearningProgressPoints(theWorldTimeStopBlink.get().getBaseTimeStop(), 
		    						(int) (((TimeStopperStandStats) stats).timeStopLearningPerTick * timeStopTicks));
		    			}
	    			}
	    		}
	    	}
    	}
    }
    
    @Override
    public void afterAttack(Entity target, StandAttackProperties punch, StandEntity stand, IStandPower power, LivingEntity user, boolean hurt, boolean killed) {
		if (killed && user != null && stand.distanceToSqr(user) > 16) {
			JojoModUtil.sayVoiceLine(user, ModSounds.DIO_THIS_IS_THE_WORLD.get());
		}
    }
    
    @Override
    public float getStaminaCost(IStandPower stand) {
        return theWorldHeavyAttack.get().getStaminaCost(stand);
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackRecovery(standEntity.getAttackSpeed());
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
    	standEntity.punch(this, getPunch(), task.getTarget());
    	userPower.getUser().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.hasUsedTimeStopToday = true);
    }
    
    @Override
    public boolean noAdheringToUserOffset(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    @Override
    public boolean noComboDecay() {
        return true;
    }
    
    @Override
	protected boolean cancels(StandEntityAction currentAction, IStandPower standPower, StandEntity standEntity, Phase currentPhase) {
        return currentAction != this && currentPhase == Phase.RECOVERY;
    }
}
