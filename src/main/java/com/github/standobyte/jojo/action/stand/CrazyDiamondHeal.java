package com.github.standobyte.jojo.action.stand;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.IHasHealth;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CrazyDiamondHeal extends StandEntityAction {

    public CrazyDiamondHeal(StandEntityAction.Builder builder) {
        super(builder);
        friendlyFire = true;
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
    public boolean standCanTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        return task.getTarget().getType() == TargetType.ENTITY;
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        Entity targetEntity = task.getTarget().getEntity();
        
        boolean healedThisTick = false;
        
        if (targetEntity instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) targetEntity;
            healedThisTick = healLivingEntity(world, targetLiving, standEntity);
        }
        else if (targetEntity instanceof IHasHealth) {
            healedThisTick = heal(world, targetEntity, (IHasHealth) targetEntity, 
                    (e, clientSide) -> {
                        if (!clientSide) {
                            e.setHealth(e.getHealth() + e.getMaxHealth() / 40 * (float) healingSpeed(standEntity));
                        }
                    },
                    e -> e.getHealth() < e.getMaxHealth());
        }
        else if (targetEntity instanceof BoatEntity) {
            healedThisTick = heal(world, targetEntity, (BoatEntity) targetEntity, 
                    (e, clientSide) -> {
                        if (!clientSide) {
                            e.setDamage(Math.max(e.getDamage() - (float) healingSpeed(standEntity), 0));
                        }
                    },
                    e -> e.getDamage() > 0);
        }
        

        if (!world.isClientSide()) {
            barrageVisualsTick(standEntity, healedThisTick, targetEntity != null ? targetEntity.getBoundingBox().getCenter() : null);
        }
    }

    public static double healingSpeed(StandEntity standEntity) {
        return standEntity.getAttackSpeed() * 0.05F + 0.55;
    }

    public static boolean healLivingEntity(World world, LivingEntity entity, StandEntity standEntity) {
        // FIXME disable it if the target is a dead body already
        if (entity.deathTime > 0) {
            boolean resolveEffect = standEntity.getUser() != null && standEntity.getUser().hasEffect(ModStatusEffects.RESOLVE.get());
            if (!resolveEffect && entity.deathTime > 1 || entity.deathTime > 15) {
                return false;
            }
            return heal(world, entity, 
                    entity, (e, clientSide) -> {
                        LivingEntity toHeal = e;
                        toHeal = StandUtil.getStandUser(e);
                        toHeal.deathTime = Math.max(toHeal.deathTime - 2, 0);
                        e.deathTime = toHeal.deathTime;
                        if (!clientSide && toHeal.deathTime <= 0) {
                            toHeal.setHealth(0.001F);
                            MCUtil.onEntityResurrect(toHeal);
                        }
                    }, e -> true);
        }
        
        return heal(world, entity, 
                entity, (e, clientSide) -> {
                    if (!clientSide) {
                        LivingEntity toHeal = StandUtil.getStandUser(e);
                        toHeal.setHealth(toHeal.getHealth() + 0.5F * (float) healingSpeed(standEntity));
                    }
                }, 
                e -> e.getHealth() < e.getMaxHealth());
    }
    
    public static <T> boolean heal(World world, Entity entity, T entityCasted, BiConsumer<T, Boolean> heal, Predicate<T> isHealthMissing) {
        boolean healed = isHealthMissing.test(entityCasted);
        heal.accept(entityCasted, world.isClientSide());
        if (world.isClientSide() && isHealthMissing.test(entityCasted) && ClientUtil.canSeeStands()) {
            addParticlesAround(entity);
        }
        return healed;
    }
    
    public static void addParticlesAround(Entity entity) {
        if (entity.level.isClientSide() && ClientUtil.canSeeStands()) {
            int particlesCount = Math.max(MathHelper.ceil(entity.getBbWidth() * (entity.getBbHeight() * 2 * entity.getBbHeight())), 1);
            for (int i = 0; i < particlesCount; i++) {
                entity.level.addParticle(ModParticles.CD_RESTORATION.get(), entity.getRandomX(1), entity.getRandomY(), entity.getRandomZ(1), 0, 0, 0);
            }
        }
    }
    
    @Override
    public void phaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {
        if (world.isClientSide()) {
            if (to == Phase.PERFORM) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
                        ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, Phase.PERFORM, 1.0F, 1.0F, true);
            }
            else if (from == Phase.PERFORM) {
                standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    @Override
    protected boolean barrageVisuals(StandEntity standEntity, IStandPower standPower, StandEntityTask task) {
        if (!super.barrageVisuals(standEntity, standPower, task)) return false;
        
        if (standPower.getUser() != null && standPower.getUser().hasEffect(ModStatusEffects.RESOLVE.get())) {
            return true;
        }
        
        ActionTarget target = task.getTarget();
        if (target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) target.getEntity();
            return targetLiving.getHealth() / targetLiving.getMaxHealth() <= 0.5F;
        }
        return false;
    }

    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return offsetToTarget(standPower, standEntity, task, 0, standEntity.getMaxEffectiveRange(), null)
                .orElse(super.getOffsetFromUser(standPower, standEntity, task));
    }
}
