package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class CommonReflection {
    private static final Field GOAL_SELECTOR_AVALIABLE_GOALS = ObfuscationReflectionHelper.findField(GoalSelector.class, "field_220892_d");
    public static Set<PrioritizedGoal> getGoalsSet(GoalSelector targetGoals) {
        return ReflectionUtil.getFieldValue(GOAL_SELECTOR_AVALIABLE_GOALS, targetGoals);
    }

    private static final Field NEAREST_TARGET_GOAL_TARGET_TYPE = ObfuscationReflectionHelper.findField(NearestAttackableTargetGoal.class, "field_75307_b");
    public static Class<? extends LivingEntity> getTargetClass(NearestAttackableTargetGoal<?> goal) {
        return ReflectionUtil.getFieldValue(NEAREST_TARGET_GOAL_TARGET_TYPE, goal);
    }

    private static final Field NEAREST_TARGET_GOAL_TARGET_CONDITIONS = ObfuscationReflectionHelper.findField(NearestAttackableTargetGoal.class, "field_220779_d");
    public static EntityPredicate getTargetConditions(NearestAttackableTargetGoal<?> goal) {
        return ReflectionUtil.getFieldValue(NEAREST_TARGET_GOAL_TARGET_CONDITIONS, goal);
    }

    public static void setTargetConditions(NearestAttackableTargetGoal<?> goal, EntityPredicate conditions) {
        ReflectionUtil.setFieldValue(NEAREST_TARGET_GOAL_TARGET_CONDITIONS, goal, conditions);
    }

    private static final Field ENTITY_PREDICATE_SELECTOR = ObfuscationReflectionHelper.findField(EntityPredicate.class, "field_221023_h");
    public static Predicate<LivingEntity> getTargetSelector(EntityPredicate conditions) {
        return ReflectionUtil.getFieldValue(ENTITY_PREDICATE_SELECTOR, conditions);
    }

    private static final Method TARGET_GOAL_GET_FOLLOW_DISTANCE = ObfuscationReflectionHelper.findMethod(TargetGoal.class, "func_111175_f");
    public static double getTargetDistance(NearestAttackableTargetGoal<?> goal) {
        return ReflectionUtil.invokeMethod(TARGET_GOAL_GET_FOLLOW_DISTANCE, goal);
    }
    
    

    private static final Field CREEPER_ENTITY_SWELL = ObfuscationReflectionHelper.findField(CreeperEntity.class, "field_70833_d");
    public static void setCreeperSwell(CreeperEntity entity, int swell) {
        ReflectionUtil.setFieldValue(CREEPER_ENTITY_SWELL, entity, swell);
    }
    
    

    private static final Field PROJECTILE_ENTITY_LEFT_OWNER = ObfuscationReflectionHelper.findField(ProjectileEntity.class, "field_234611_d_");
    public static boolean getProjectileLeftOwner(ProjectileEntity entity) {
        return ReflectionUtil.getFieldValue(PROJECTILE_ENTITY_LEFT_OWNER, entity);
    }
    
    
    
    private static final Method CHUNK_GENERATOR_CODEC = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
    public static Codec<? extends ChunkGenerator> getCodec(ChunkGenerator chunkGenerator) {
        return ReflectionUtil.invokeMethod(CHUNK_GENERATOR_CODEC, chunkGenerator);
    }
    
    private static final Field FLAT_GENERATION_SETTING_STRUCTURE_FEATURES = ObfuscationReflectionHelper.findField(
            FlatGenerationSettings.class, "field_202247_j");
    public static Map<Structure<?>, StructureFeature<?, ?>> flatGenSettingsStructures() {
        return ReflectionUtil.getFieldValue(FLAT_GENERATION_SETTING_STRUCTURE_FEATURES, null);
    }
}
