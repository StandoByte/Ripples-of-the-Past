package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
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
    protected StandEntityActionModifier getRecoveryFollowup(IStandPower standPower, StandEntity standEntity) {
        if (standEntity == null) return null;
        
        if (standEntity.getCurrentTask().map(task -> task.getTarget().getType() == TargetType.ENTITY).orElse(false)) {
            return super.getRecoveryFollowup(standPower, standEntity);
        }
        
        return null;
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
                LivingEntity targetLiving = (LivingEntity) target;
                targetLiving = StandUtil.getStandUser(targetLiving);
                ObjectEntity.Type toothType = GoldExperienceToothLifeform.getToothObject(targetLiving);
                if (toothType != null) {
                    ObjectEntity tooth = new ObjectEntity(world, toothType);
                    tooth.setPos(targetLiving.getX(), targetLiving.getEyeY(), targetLiving.getZ());
                    tooth.setOwner(targetLiving.getUUID());
                    
                    float xRot = -37.5F - 15 * stand.getRandom().nextFloat();
                    float yRot = 90 + 30 * stand.getRandom().nextFloat();
                    Vector3d toothVec = Vector3d.directionFromRotation(
                            target.xRot + xRot, 
                            target.yRot + yRot);
                    tooth.setDeltaMovement(toothVec.scale(Math.max(strength, 2) * 0.05));
                    world.addFreshEntity(tooth);
    
                    task.getAdditionalData().push(Integer.class, tooth.getId());
                }
            }
        }
    }
}
