package com.github.standobyte.jojo.action.stand;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.IHasHealth;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CrazyDiamondHeal extends StandEntityAction {

    public CrazyDiamondHeal(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        Entity targetEntity = target.getEntity();
        if (targetEntity.is(user)) {
            return conditionMessage("cd_heal_self");
        }
        if (!(targetEntity instanceof LivingEntity
                || targetEntity instanceof IHasHealth
                || targetEntity instanceof BoatEntity)) {
            return conditionMessage("heal_target");
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        Entity targetEntity = task.getTarget().getEntity();
        if (targetEntity != null) {
            if (targetEntity instanceof LivingEntity) {
                handle(world, targetEntity, 
                        (LivingEntity) targetEntity, e -> e.setHealth(e.getHealth() + 0.25F), 
                        e -> e.getHealth() < e.getMaxHealth());
            }
            else if (targetEntity instanceof IHasHealth) {
                handle(world, targetEntity, 
                        (IHasHealth) targetEntity, e -> e.setHealth(e.getHealth() + e.getMaxHealth() / 40), 
                        e -> e.getHealth() < e.getMaxHealth());
            }
            else if (targetEntity instanceof BoatEntity) {
                handle(world, targetEntity, 
                        (BoatEntity) targetEntity, e -> e.setDamage(Math.max(e.getDamage() - 1, 0)), 
                        e -> e.getDamage() > 0);
            }
        }
        else {
            JojoMod.LOGGER.debug("!!!!!!!!!!! empty target !!!!!!!!!!");
        }
    }
    
    private <T> void handle(World world, Entity entity, T entityCasted, Consumer<T> heal, Predicate<T> healthMissing) {
        if (!world.isClientSide()) {
            heal.accept(entityCasted);
        }
        else if (healthMissing.test(entityCasted)) {
            int particlesCount = Math.max(MathHelper.ceil(entity.getBbWidth() * (entity.getBbHeight() * 2 * entity.getBbHeight())), 1);
            for (int i = 0; i < particlesCount; i++) {
                world.addParticle(ModParticles.CD_RESTORATION.get(), entity.getRandomX(1), entity.getRandomY(), entity.getRandomZ(1), 0, 0, 0);
            }
        }
    }

    // FIXME !!!!!!!!!!!!!!! yknow there should be a target range
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;    
    }
}
