package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
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
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            boolean shift = isShiftVariation();
            int emeralds = shift ? 2 : 1;
            for (int i = 0; i < emeralds; i++) {
                HGEmeraldEntity emerald = new HGEmeraldEntity(standEntity, world, userPower);
                emerald.setConcentrated(shift);
                standEntity.shootProjectile(emerald, shift ? 1.5F : 1F, shift ? 2.0F : 8.0F);
            }
            
            HierophantGreenEntity hierophant = (HierophantGreenEntity) standEntity;
            int barriers = hierophant.getPlacedBarriersCount();
            if (barriers > 0) {
                RayTraceResult rayTrace = JojoModUtil.rayTrace(hierophant.isManuallyControlled() ? standEntity : hierophant.getUser(), 
                        standEntity.getMaxRange(), entity -> entity instanceof LivingEntity && hierophant.canAttack((LivingEntity) entity));
                if (rayTrace.getType() != RayTraceResult.Type.MISS) {
                    hierophant.shootEmeraldsFromBarriers(rayTrace.getLocation(), 1);
                }
            }
        }
    }
    
    @Override
    public void onMaxTraining(IStandPower power) {
        power.unlockAction(getShiftVariationIfPresent());
    }
}
