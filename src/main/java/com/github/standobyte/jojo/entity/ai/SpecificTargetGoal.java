package com.github.standobyte.jojo.entity.ai;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.world.server.ServerWorld;

public class SpecificTargetGoal extends TargetGoal {
    private final UUID targetUuid;
    private LivingEntity targetEntity;

    public SpecificTargetGoal(MobEntity mob, UUID target, boolean mustSee, boolean mustReach) {
        super(mob, mustSee, mustReach);
        this.targetUuid = target;
    }
    
    @Nullable
    private LivingEntity getTargetEntity() {
        if (targetUuid == null) return null;
        
        if (targetEntity != null) {
            if (targetEntity.isAlive()) {
                return targetEntity;
            }
            else {
                targetEntity = null;
            }
        }
        
        if (!mob.level.isClientSide()) {
            Entity entity = ((ServerWorld) mob.level).getEntity(targetUuid);
            if (entity instanceof LivingEntity) {
                targetEntity = (LivingEntity) entity;
            }
        }
        
        return targetEntity;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = getTargetEntity();
        return target != null && target.isAlive();
    }
    
    @Override
    public void start() {
        LivingEntity target = getTargetEntity();
        if (target != null) {
            mob.setTarget(target);
            this.targetMob = mob.getTarget();
        }
        super.start();
    }

}
