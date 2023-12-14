package com.github.standobyte.jojo.util.mc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class CommonReflection {
    private static final Field GOAL_SELECTOR_AVAILABLE_GOALS = ObfuscationReflectionHelper.findField(GoalSelector.class, "field_220892_d");
    public static Set<PrioritizedGoal> getGoalsSet(GoalSelector targetGoals) {
        return ReflectionUtil.getFieldValue(GOAL_SELECTOR_AVAILABLE_GOALS, targetGoals);
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

    private static final Field HURT_BY_TARGET_GOAL_TO_IGNORE = ObfuscationReflectionHelper.findField(HurtByTargetGoal.class, "field_179447_c");
    public static Class<?>[] getToIgnoreDamage(HurtByTargetGoal goal) {
        return ReflectionUtil.getFieldValue(HURT_BY_TARGET_GOAL_TO_IGNORE, goal);
    }
    
    

    private static final Field CREEPER_ENTITY_SWELL = ObfuscationReflectionHelper.findField(CreeperEntity.class, "field_70833_d");
    public static void setCreeperSwell(CreeperEntity entity, int swell) {
        ReflectionUtil.setIntFieldValue(CREEPER_ENTITY_SWELL, entity, swell);
    }
    
    

    private static final Field PROJECTILE_ENTITY_LEFT_OWNER = ObfuscationReflectionHelper.findField(ProjectileEntity.class, "field_234611_d_");
    public static boolean getProjectileLeftOwner(ProjectileEntity entity) {
        return ReflectionUtil.getBooleanFieldValue(PROJECTILE_ENTITY_LEFT_OWNER, entity);
    }
    
    
    
    private static final Method CHUNK_GENERATOR_CODEC = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
    public static Codec<? extends ChunkGenerator> getCodec(ChunkGenerator chunkGenerator) {
        return ReflectionUtil.invokeMethod(CHUNK_GENERATOR_CODEC, chunkGenerator);
    }
    
    private static final Field FLAT_GENERATION_SETTING_STRUCTURE_FEATURES = ObfuscationReflectionHelper.findField(FlatGenerationSettings.class, "field_202247_j");
    public static Map<Structure<?>, StructureFeature<?, ?>> flatGenSettingsStructures() {
        return ReflectionUtil.getFieldValue(FLAT_GENERATION_SETTING_STRUCTURE_FEATURES, null);
    }
    
    
    
    private static final Field CRAFTING_INVENTORY_MENU = ObfuscationReflectionHelper.findField(CraftingInventory.class, "field_70465_c");
    public static Container getCraftingInventoryMenu(CraftingInventory inventory) {
        return ReflectionUtil.getFieldValue(CRAFTING_INVENTORY_MENU, inventory);
    }
    
    private static final Field PLAYER_CONTAINER_OWNER = ObfuscationReflectionHelper.findField(PlayerContainer.class, "field_82862_h");
    public static PlayerEntity getPlayer(PlayerContainer container) {
        return ReflectionUtil.getFieldValue(PLAYER_CONTAINER_OWNER, container);
    }
    
    private static final Field WORKBENCH_CONTAINER_PLAYER = ObfuscationReflectionHelper.findField(WorkbenchContainer.class, "field_192390_i");
    public static PlayerEntity getPlayer(WorkbenchContainer container) {
        return ReflectionUtil.getFieldValue(WORKBENCH_CONTAINER_PLAYER, container);
    }
    
    
    
    private static final Field FURNACE_TE_LIT_TIME = ObfuscationReflectionHelper.findField(AbstractFurnaceTileEntity.class, "field_214018_j");
    public static int getFurnaceLitTime(AbstractFurnaceTileEntity tileEntity) {
        return ReflectionUtil.getIntFieldValue(FURNACE_TE_LIT_TIME, tileEntity);
    }
    
    public static void setFurnaceLitTime(AbstractFurnaceTileEntity tileEntity, int ticks) {
        ReflectionUtil.setIntFieldValue(FURNACE_TE_LIT_TIME, tileEntity, ticks);
    }
    
    private static final Field FURNACE_TE_LIT_DURATION = ObfuscationReflectionHelper.findField(AbstractFurnaceTileEntity.class, "field_214019_k");
    public static void setFurnaceLitDuration(AbstractFurnaceTileEntity tileEntity, int ticks) {
        ReflectionUtil.setIntFieldValue(FURNACE_TE_LIT_DURATION, tileEntity, ticks);
    }
    
    
    
    private static final Field LIVING_ENTITY_LERP_STEPS = ObfuscationReflectionHelper.findField(LivingEntity.class, "field_70716_bi");
    public static int getLerpSteps(LivingEntity entity) {
        return ReflectionUtil.getIntFieldValue(LIVING_ENTITY_LERP_STEPS, entity);
    }
    
    public static void setLerpSteps(LivingEntity entity, int steps) {
        ReflectionUtil.setIntFieldValue(LIVING_ENTITY_LERP_STEPS, entity, steps);
    }
    
    
    
    private static final Field FIREWORK_ROCKET_ENTITY_LIFETIME = ObfuscationReflectionHelper.findField(FireworkRocketEntity.class, "field_92055_b");
    public static void setLifetime(FireworkRocketEntity entity, int ticks) {
        ReflectionUtil.setIntFieldValue(FIREWORK_ROCKET_ENTITY_LIFETIME, entity, ticks);
    }
    
    private static final Field FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM_FIELD = ObfuscationReflectionHelper.findField(FireworkRocketEntity.class, "field_184566_a");
    private static DataParameter<ItemStack> FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM = null;
    public static DataParameter<ItemStack> getFireworkItemParameter() {
        if (FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM == null) {
            FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM = ReflectionUtil.getFieldValue(FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM_FIELD, null);
        }
        return FIREWORK_ROCKET_ENTITY_DATA_ID_FIREWORKS_ITEM;
    }
    
    private static final Field CREEPER_DATA_IS_POWERED_FIELD = ObfuscationReflectionHelper.findField(CreeperEntity.class, "field_184714_b");
    private static DataParameter<Boolean> CREEPER_DATA_IS_POWERED = null;
    public static DataParameter<Boolean> getCreeperPoweredParameter() {
        if (CREEPER_DATA_IS_POWERED == null) {
            CREEPER_DATA_IS_POWERED = ReflectionUtil.getFieldValue(CREEPER_DATA_IS_POWERED_FIELD, null);
        }
        return CREEPER_DATA_IS_POWERED;
    }
    
    private static final Field EXPLOSION_RADIUS = ObfuscationReflectionHelper.findField(Explosion.class, "field_77280_f");
    public static float getRadius(Explosion explosion) {
        return ReflectionUtil.getFloatFieldValue(EXPLOSION_RADIUS, explosion);
    }
    
    
    
    private static final Field LIVING_ENTITY_ATTACK_STRENGTH_TICKER = ObfuscationReflectionHelper.findField(LivingEntity.class, "field_184617_aD");
    public static int getAttackStrengthTicker(LivingEntity entity) {
        return ReflectionUtil.getIntFieldValue(LIVING_ENTITY_ATTACK_STRENGTH_TICKER, entity);
    }
    
    public static void setAttackStrengthTicker(LivingEntity entity, int attackStrengthTicker) {
        ReflectionUtil.setIntFieldValue(LIVING_ENTITY_ATTACK_STRENGTH_TICKER, entity, attackStrengthTicker);
    }
    
    
    
    private static final Field PLAYER_ENTITY_SLEEP_COUNTER = ObfuscationReflectionHelper.findField(PlayerEntity.class, "field_71076_b");
    public static void setSleepCounter(PlayerEntity entity, int sleepCounter) {
        ReflectionUtil.setIntFieldValue(PLAYER_ENTITY_SLEEP_COUNTER, entity, sleepCounter);
    }
    
    

    private static final Field MERCHANT_CONTAINER_TRADER = ObfuscationReflectionHelper.findField(MerchantContainer.class, "field_75178_e");
    public static IMerchant getTrader(MerchantContainer merchantContainer) {
        return ReflectionUtil.getFieldValue(MERCHANT_CONTAINER_TRADER, merchantContainer);
    }
    
    
    
    private static final Method PROJECTILE_ITEM_ENTITY_GET_ITEM_RAW = ObfuscationReflectionHelper.findMethod(ProjectileItemEntity.class, "func_213882_k");
    public static ItemStack getItemRaw(ProjectileItemEntity entity) {
        return ReflectionUtil.invokeMethod(PROJECTILE_ITEM_ENTITY_GET_ITEM_RAW, entity);
    }
    
    private static final Method PROJECTILE_ITEM_ENTITY_GET_DEFAULT_ITEM = ObfuscationReflectionHelper.findMethod(ProjectileItemEntity.class, "func_213885_i");
    public static Item getDefaultItem(ProjectileItemEntity entity) {
        return ReflectionUtil.invokeMethod(PROJECTILE_ITEM_ENTITY_GET_DEFAULT_ITEM, entity);
    }
    
    
    
    private static final Field MOOSHROOM_ENTITY_EFFECT = ObfuscationReflectionHelper.findField(MooshroomEntity.class, "field_213450_bA");
    public static Effect getEffect(MooshroomEntity entity) {
        return ReflectionUtil.getFieldValue(MOOSHROOM_ENTITY_EFFECT, entity);
    }
    
    private static final Field MOOSHROOM_ENTITY_EFFECT_DURATION = ObfuscationReflectionHelper.findField(MooshroomEntity.class, "field_213447_bB");
    public static int getEffectDuration(MooshroomEntity entity) {
        return ReflectionUtil.getIntFieldValue(MOOSHROOM_ENTITY_EFFECT_DURATION, entity);
    }
    
    public static void clearEffect(MooshroomEntity entity) {
        ReflectionUtil.setFieldValue(MOOSHROOM_ENTITY_EFFECT, entity, null);
        ReflectionUtil.setIntFieldValue(MOOSHROOM_ENTITY_EFFECT_DURATION, entity, 0);
    }
    
    
    
    private static final Method ZOMBIE_VILLAGER_ENTITY_START_CONVERTING = ObfuscationReflectionHelper.findMethod(ZombieVillagerEntity.class, "func_191991_a", UUID.class, int.class);
    public static void startConverting(ZombieVillagerEntity entity, @Nullable UUID conversionStarter, int villagerConversionTime) {
        ReflectionUtil.invokeMethod(ZOMBIE_VILLAGER_ENTITY_START_CONVERTING, entity, 
                conversionStarter, villagerConversionTime);
    }
    
    
    
    private static final Field ENTITY_DATA_CUSTOM_NAME_FIELD = ObfuscationReflectionHelper.findField(Entity.class, "field_184242_az");
    private static DataParameter<Optional<ITextComponent>> ENTITY_DATA_CUSTOM_NAME = null;
    public static DataParameter<Optional<ITextComponent>> getEntityCustomNameParameter() {
        if (ENTITY_DATA_CUSTOM_NAME == null) {
            ENTITY_DATA_CUSTOM_NAME = ReflectionUtil.getFieldValue(ENTITY_DATA_CUSTOM_NAME_FIELD, null);
        }
        return ENTITY_DATA_CUSTOM_NAME;
    }
}
