package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class GoldExperienceHeavyPunch extends StandEntityHeavyAttack {

    public GoldExperienceHeavyPunch(Builder builder) {
        super(builder);
    }
    
    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        StandEntityPunch punch = new ToothKnockingHeavyPunch(stand, target, dmgSource);
        punch.copyProperties(super.punchEntity(stand, target, dmgSource));
        return punch;
    }

    @Override
    public void rotateStandTowardsTarget(StandEntity standEntity, ActionTarget target, StandEntityTask task) {
        if (task.getTarget().getType() == TargetType.ENTITY) {
            Entity entity = task.getTarget().getEntity();
            Vector3d pos = new Vector3d(entity.getX(), entity.getY(1.0), entity.getZ());
            MCUtil.rotateTowards(standEntity, pos, 360F);
            return;
        }
        
        super.rotateStandTowardsTarget(standEntity, target, task);
    }
    
    public static class ToothKnockingHeavyPunch extends HeavyPunchInstance {

        public ToothKnockingHeavyPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
            super(stand, target, dmgSource);
        }

        @Override
        protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
            super.afterAttack(stand, target, dmgSource, task, hurt, killed);
            double strength = stand.getAttackDamage();
            World world = stand.level;
            if (!world.isClientSide() && hurt && target instanceof LivingEntity) {
                LivingEntity targetLiving = StandUtil.getStandUser((LivingEntity) target);
                ObjectEntity tooth = new ObjectEntity(world, ObjectEntity.Type.TOOTH);
                tooth.setPos(targetLiving.getX(), targetLiving.getEyeY(), targetLiving.getZ());
                tooth.setOwner(targetLiving.getUUID());
                
                Vector3d targetLookVec = target.getLookAngle();
                float xRot = 45 + 15 * stand.getRandom().nextFloat();
                float yRot = 75 + 30 * stand.getRandom().nextFloat();
                tooth.setDeltaMovement(
                        targetLookVec
                        .xRot(xRot * MathUtil.DEG_TO_RAD)
                        .yRot(-yRot * MathUtil.DEG_TO_RAD)
                        .scale(Math.max(strength, 2) * 0.05));
                world.addFreshEntity(tooth);
            }
        }
    }
}
