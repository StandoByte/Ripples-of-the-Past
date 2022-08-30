package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondDisfigure.TargetHitPart;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondDisfiguringPunch extends StandEntityHeavyAttack {

    public CrazyDiamondDisfiguringPunch(Builder builder) {
        super(builder);
    }
    
//    @Override
//    protected StandEntityActionModifier getRecoveryFollowup(IStandPower standPower, StandEntity standEntity) {
//        return ModActions.CRAZY_DIAMOND_DISFIGURE.get();
//    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        super.onTaskSet(world, standEntity, standPower, phase, task, ticks);
        if (task.getTarget().getType() == TargetType.ENTITY) {
            // FIXME !!!!! (combo heavy) determine the body part aimed at 
            // FIXME !!!!! (combo heavy) and it probably should be synced somehow
            TargetHitPart hitPart = TargetHitPart.getHitTarget(null);
            task.getAdditionalData().push(TargetHitPart.class, hitPart);
        }
    }

    @Override
    public void rotateStandTowardsTarget(StandEntity standEntity, ActionTarget target, StandEntityTask task) {
        if (task.getTarget().getType() == TargetType.ENTITY && !task.getAdditionalData().isEmpty(TargetHitPart.class)) {
            Entity entity = task.getTarget().getEntity();
            if (entity instanceof LivingEntity) {
                TargetHitPart hitPart = task.getAdditionalData().peek(TargetHitPart.class);
                if (hitPart != null) {
                    Vector3d pos = hitPart.getPartCenter((LivingEntity) entity);
                    if (pos != null) {
                        JojoModUtil.rotateTowards(standEntity, pos, 360F);
                        return;
                    }
                }
            }
        }
        super.rotateStandTowardsTarget(standEntity, target, task);
    }
}
