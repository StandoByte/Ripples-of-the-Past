package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.config.ActionConfigField;
import com.github.standobyte.jojo.action.stand.effect.CDTurnIntoAngeloRockEffect;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class CrazyDiamondAngeloRock extends StandEntityActionModifier {
    @ActionConfigField private boolean keepMobsInside = true;

    public CrazyDiamondAngeloRock(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (!JojoModUtil.breakingBlocksEnabled(user.level)) {
            return ActionConditionResult.NEGATIVE;
        }
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
                if (!StandEffectsTracker.getEffectOfType(targetEntity, ModStandEffects.TURN_INTO_ANGELO_ROCK.get()).isPresent()) {
                    CDTurnIntoAngeloRockEffect effect = new CDTurnIntoAngeloRockEffect();
                    effect.withTarget(targetEntity);
                    effect.keepMobsInside = this.keepMobsInside;
                    userPower.getContinuousEffects().addEffect(effect);
                }
            }
        }
    }
    
    @Override
    public boolean makesAttackNonLethal(LivingEntity target) {
        return true;
    }
}
