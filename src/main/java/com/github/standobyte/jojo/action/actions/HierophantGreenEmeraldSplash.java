package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.JojoModUtil;

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
            float damageReduction = (float) standEntity.getRangeEfficiency();
            int emeralds = shift ? 2 : 1;
            for (int i = 0; i < emeralds; i++) {
                HGEmeraldEntity emeraldEntity = new HGEmeraldEntity(standEntity, world);
                emeraldEntity.setDamageFactor(damageReduction);
                emeraldEntity.shootFromRotation(standEntity, shift ? 1.25F : 0.75F, shift ? 2.0F : 8.0F);
                world.addFreshEntity(emeraldEntity);
            }
            
            HierophantGreenEntity hierophant = (HierophantGreenEntity) standEntity;
            int barriers = hierophant.getPlacedBarriersCount();
            if (barriers > 0) {
                RayTraceResult rayTrace = JojoModUtil.rayTrace(hierophant.isManuallyControlled() ? standEntity : hierophant.getUser(), 
                        standEntity.getMaxRange(), entity -> entity instanceof LivingEntity && hierophant.canAttack((LivingEntity) entity));
                if (rayTrace.getType() != RayTraceResult.Type.MISS) {
                    hierophant.shootEmeraldsFromBarriers(rayTrace.getLocation(), shift, 1);
                }
            }
        }
    }
}
