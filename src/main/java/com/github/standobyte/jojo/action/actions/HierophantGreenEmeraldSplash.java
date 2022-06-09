package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class HierophantGreenEmeraldSplash extends StandEntityAction {

    public HierophantGreenEmeraldSplash(StandEntityAction.Builder builder) {
        super(builder);
    }
//    
//    @Override
//    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
//    	if (power.getStandManifestation() instanceof HierophantGreenEntity) {
//    		HierophantGreenEntity stand = (HierophantGreenEntity) power.getStandManifestation();
//    		if (stand.getPlacedBarriersCount() > 0) {
//    			RayTraceResult aimTarget = aimForBarriers(stand);
//				if (aimTarget.getType() == RayTraceResult.Type.ENTITY && stand.getBarriersNet().doBarriersSurround(aimTarget.getLocation())) {
//		    		return ModSounds.KAKYOIN_20M_EMERALD_SPLASH.get();
//				}
//    		}
//    	}
//    	return super.getShout(user, power, target, wasActive);
//    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            boolean shift = isShiftVariation();
            double fireRate = standEntity.getAttackSpeed() / userPower.getType().getDefaultStats().getBaseAttackSpeed();
            if (shift) fireRate *= 2;
        	JojoModUtil.doFractionTimes(() -> {
                HGEmeraldEntity emerald = new HGEmeraldEntity(standEntity, world, userPower);
                emerald.setConcentrated(shift);
                standEntity.shootProjectile(emerald, shift ? 1.5F : 1F, shift ? 2.0F : 8.0F);
        	}, fireRate);
            
            HierophantGreenEntity hierophant = (HierophantGreenEntity) standEntity;
            int barriers = hierophant.getPlacedBarriersCount();
            if (barriers > 0) {
                RayTraceResult rayTrace = aimForBarriers(hierophant);
                if (rayTrace.getType() != RayTraceResult.Type.MISS) {
                    hierophant.shootEmeraldsFromBarriers(rayTrace.getLocation(), task.getTick());
                }
            }
        }
    }
    
    private RayTraceResult aimForBarriers(HierophantGreenEntity stand) {
    	return JojoModUtil.rayTrace(stand.isManuallyControlled() ? stand : stand.getUser(), 
    			stand.getMaxRange(), entity -> entity instanceof LivingEntity && stand.canAttack((LivingEntity) entity));
    }
    
    @Override
    public void onMaxTraining(IStandPower power) {
        power.unlockAction(getShiftVariationIfPresent());
    }
}
