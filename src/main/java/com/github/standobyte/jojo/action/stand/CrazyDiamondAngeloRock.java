package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.effect.CDTurnIntoAngeloRockEffect;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class CrazyDiamondAngeloRock extends StandEntityActionModifier {

    public CrazyDiamondAngeloRock(Builder builder) {
        super(builder);
    }
    
    // TODO (angelo) lock the action if the stand can't break blocks due to config
    // TODO (angelo) limit the entity types that can be affected by this
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive() && target.getEntity() instanceof LivingEntity) {
            StandEntity standEntity = (StandEntity) power.getStandManifestation();
            return ActionConditionResult.noMessage(hasTaskWithNoModifiers(standEntity));
        }
        return ActionConditionResult.NEGATIVE;
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide() || task.getTarget().getType() != TargetType.ENTITY) return;
        Entity entity = task.getTarget().getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity targetEntity = (LivingEntity) entity;
            targetEntity = StandUtil.getStandUser(targetEntity);
            if (entity.isAlive()) {
                KnockbackCollisionImpact kbCollision = KnockbackCollisionImpact.getHandler(targetEntity).orElse(null);
                if (kbCollision == null || !kbCollision.isActive()) {
                    return;
                }
                userPower.getContinuousEffects().addEffect(new CDTurnIntoAngeloRockEffect().withTarget(targetEntity));
            }
        }
    }
    
    @Override
    public boolean makesAttackNonLethal(LivingEntity target) {
        return true;
    }
}
