package com.github.standobyte.jojo.action.stand;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
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
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        Entity targetEntity = target.getEntity();
        if (targetEntity.is(power.getUser())) {
            return conditionMessage("cd_heal_self");
        }
        if (!(targetEntity instanceof LivingEntity
                || targetEntity instanceof IHasHealth
                || targetEntity instanceof BoatEntity)) {
            return conditionMessage("heal_target");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;    
    }
    
    @Override
    public boolean standCanTick(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        return task.getTarget().getType() == TargetType.ENTITY;
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        Entity targetEntity = task.getTarget().getEntity();
        if (targetEntity instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) targetEntity;
            // FIXME disable it if the target is a dead body already
            if (targetLiving.deathTime > 0) {
                handle(world, targetEntity, targetLiving, 
                        (e, clientSide) -> {
                            if (targetLiving.deathTime == 1) {
                                if (!clientSide) {
                                    targetLiving.setHealth(0.001F);
                                }
                                targetLiving.deathTime--;
                            }
                        },
                        e -> e.getHealth() < e.getMaxHealth());
            }
            else {
                handleLivingEntity(world, targetLiving);
            }
        }
        else if (targetEntity instanceof IHasHealth) {
            handle(world, targetEntity, (IHasHealth) targetEntity, 
                    (e, clientSide) -> {
                        if (!clientSide) {
                            e.setHealth(e.getHealth() + e.getMaxHealth() / 40);
                        }
                    },
                    e -> e.getHealth() < e.getMaxHealth());
        }
        else if (targetEntity instanceof BoatEntity) {
            handle(world, targetEntity, (BoatEntity) targetEntity, 
                    (e, clientSide) -> {
                        if (clientSide) {
                            e.setDamage(Math.max(e.getDamage() - 1, 0));
                        }
                    },
                    e -> e.getDamage() > 0);
        }
    }

    public static boolean handleLivingEntity(World world, LivingEntity entity) {
        return handle(world, entity, 
                entity, (e, clientSide) -> {
                    if (!clientSide) {
                        e.setHealth(e.getHealth() + 0.5F);
                    }
                }, 
                e -> e.getHealth() < e.getMaxHealth());
    }

    // FIXME ! (heal) CD restore sound
    public static <T> boolean handle(World world, Entity entity, T entityCasted, BiConsumer<T, Boolean> heal, Predicate<T> isHealthMissing) {
        heal.accept(entityCasted, world.isClientSide());
        if (world.isClientSide() && isHealthMissing.test(entityCasted)) {
            addParticlesAround(entity);
        }
        return !isHealthMissing.test(entityCasted);
    }
    
    public static void addParticlesAround(Entity entity) {
        int particlesCount = Math.max(MathHelper.ceil(entity.getBbWidth() * (entity.getBbHeight() * 2 * entity.getBbHeight())), 1);
        for (int i = 0; i < particlesCount; i++) {
            entity.level.addParticle(ModParticles.CD_RESTORATION.get(), entity.getRandomX(1), entity.getRandomY(), entity.getRandomZ(1), 0, 0, 0);
        }
    }
}
