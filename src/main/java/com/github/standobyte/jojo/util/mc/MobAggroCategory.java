package com.github.standobyte.jojo.util.mc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;

public enum MobAggroCategory {
    PASSIVE,
    NEUTRAL,
    AGGRESSIVE;
    
    private static final Map<EntityType<?>, MobAggroCategory> CLASSIFICATION = new HashMap<>();
    
    @Nullable
    public static MobAggroCategory getCategoryOnServer(EntityType<?> mobType) {
        return CLASSIFICATION.computeIfAbsent(mobType, type -> {
            Entity entity = EntityTypeToInstance.getEntityInstance(type);
            if (entity instanceof MobEntity) {
                GoalSelector targetSelector = ((MobEntity) entity).targetSelector;
                Set<PrioritizedGoal> targets = CommonReflection.getGoalsSet(targetSelector);
                
                MobAggroCategory category = MobAggroCategory.PASSIVE;
                for (PrioritizedGoal target : targets) {
                    Goal targetType = target.getGoal();
                    if (targetType instanceof NearestAttackableTargetGoal) {
                        Class<?> targetClass = CommonReflection.getTargetClass((NearestAttackableTargetGoal<?>) targetType);
                        if (targetClass == PlayerEntity.class) {
                            return MobAggroCategory.AGGRESSIVE;
                        }
                    }
                    else if (targetType instanceof HurtByTargetGoal) {
                        Class<?>[] ignoredClasses = CommonReflection.getToIgnoreDamage((HurtByTargetGoal) targetType);
                        if (!ArrayUtils.contains(ignoredClasses, PlayerEntity.class)) {
                            category = MobAggroCategory.NEUTRAL;
                        }
                    }
                }
                
                return category;
            }
            return null;
        });
    }
}
