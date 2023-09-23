package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

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
//        if (power.getStandManifestation() instanceof HierophantGreenEntity) {
//            HierophantGreenEntity stand = (HierophantGreenEntity) power.getStandManifestation();
//            if (stand.getPlacedBarriersCount() > 0) {
//                RayTraceResult aimTarget = aimForBarriers(stand);
//                if (aimTarget.getType() == RayTraceResult.Type.ENTITY && stand.getBarriersNet().doBarriersSurround(aimTarget.getLocation())) {
//                    return ModSounds.KAKYOIN_20M_EMERALD_SPLASH.get();
//                }
//            }
//        }
//        return super.getShout(user, power, target, wasActive);
//    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            boolean shift = isShiftVariation();
            double fireRate = StandStatFormulas.projectileFireRateScaling(standEntity, userPower);
            if (shift) fireRate *= 2.5;
            GeneralUtil.doFractionTimes(() -> {
                HGEmeraldEntity emerald = new HGEmeraldEntity(standEntity, world, userPower);
                emerald.setBreakBlocks(shift);
                emerald.setLowerKnockback(!shift);
                standEntity.shootProjectile(emerald, shift ? 3.0F : 2.0F, shift ? 1.0F : 8.0F);
            }, fireRate);
            
            HierophantGreenEntity hierophant = (HierophantGreenEntity) standEntity;
            int barriers = hierophant.getPlacedBarriersCount();
            if (barriers > 0) {
                RayTraceResult rayTrace = aimForBarriers(hierophant);
                if (rayTrace.getType() != RayTraceResult.Type.MISS) {
                    hierophant.getBarriersNet().shootEmeraldsFromBarriers(userPower, hierophant, rayTrace.getLocation(), task.getTick(), 
                            Math.min((hierophant.getPlacedBarriersCount() / 5), 9) * hierophant.getStaminaCondition(), 
                            ModStandsInit.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get().getStaminaCostTicking(userPower) * 0.5F, 8, true);
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
        power.unlockAction((StandAction) getShiftVariationIfPresent());
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return noPhases(standPower, standEntity) || (standEntity.getCurrentTaskAction() != null
                && standEntity.getCurrentTaskAction().getShiftVariationIfPresent() == this.getShiftVariationIfPresent()) ?
                0 : StandStatFormulas.getHeavyAttackWindup(standEntity.getAttackSpeed(), 0);
    }

    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return noPhases(standPower, standEntity) ? 
                0 : super.getStandRecoveryTicks(standPower, standEntity) * 2;
    }
    
    private boolean noPhases(IStandPower standPower, StandEntity standEntity) {
        return standPower.getResolveLevel() > 2
                || standPower.getUser() != null && standPower.getUser().hasEffect(ModStatusEffects.RESOLVE.get());
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
    protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
        return phase == Phase.RECOVERY || super.isCancelable(standPower, standEntity, newAction, phase);
    }

    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return !standEntity.isArmsOnlyMode() && task.getPhase() != Phase.PERFORM ? null : super.getOffsetFromUser(standPower, standEntity, task);
    }
    
}
