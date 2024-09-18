package com.github.standobyte.jojo.action.non_stand;

import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class HamonHealing extends HamonAction {
    public static final Set<ResourceLocation> VENOM_EFFECTS_INIT = Util.make(new HashSet<>(), set -> {
        set.add(Effects.POISON.getRegistryName());
        set.add(Effects.WITHER.getRegistryName());
        set.add(Effects.HUNGER.getRegistryName());
        set.add(Effects.CONFUSION.getRegistryName());
    });

    public HamonHealing(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqStateChanged && reqFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.HAMON_HEALING.get(), 
                    1.0F, 1.0F, true, user, power, this, 15);
        }
    }
    
    // TODO bone meal
    // TODO hamon sparks only on hands when curing a target entity (for tracking players too)
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, 
            int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        float tickEnergyCost = getHeldTickEnergyCost(power);
        float hamonControl = hamon.getHamonControlLevelRatio();
        float hamonEfficiency = hamon.getActionEfficiency(tickEnergyCost, false);
        
        LivingEntity entityToHeal = user;
        if (JojoModUtil.useShiftVar(user)) {
            Entity targetEntity = target.getType() == TargetType.ENTITY && hamon.isSkillLearned(ModHamonSkills.HEALING_TOUCH.get()) ? target.getEntity() : null;
            if (targetEntity instanceof LivingEntity) {
                LivingEntity targetLiving = (LivingEntity) targetEntity;
                if (canBeHealed(targetLiving, user)) {
                    entityToHeal = targetLiving;
                }
            }
        }
        if (world.isClientSide) ClientUtil.getClientPlayer().displayClientMessage(entityToHeal.getName(), true);
        
        if (hamonEfficiency > 0) {
            if (!world.isClientSide()) {
                float qLevel = hamonControl * 3 + (hamonEfficiency - 0.75f) * 4 - 1;
                int maxLevel = MathHelper.clamp((int) qLevel, 0, 2);
                int maxTicksLeftover = (int) Math.max(((50F + hamonEfficiency * 50F) * (1 + hamonControl)), 51);
                float ticksIncreaseSpeed = 0.75f + hamonControl * 0.75f;
                int ticksInc = (int) (ticksIncreaseSpeed * ticksHeld) - (int) (ticksIncreaseSpeed * (ticksHeld - 1));
                
                int regenAmplifier = -1;
                int regenDuration = -1;
                EffectInstance regenEffect = entityToHeal.getEffect(Effects.REGENERATION);
                
                if (regenEffect == null) {
                    regenAmplifier = 0;
                    regenDuration = 51;
                }
                else if (regenEffect.getDuration() < maxTicksLeftover || regenEffect.getAmplifier() < maxLevel) {
                    if (!hamon.regenImpliedDuration.isPresent()) {
                        hamon.regenImpliedDuration = OptionalInt.of(regenEffect.getDuration());
                    }
                    int impliedDuration = hamon.regenImpliedDuration.getAsInt();
                    regenDuration = Math.min(impliedDuration + ticksInc, maxTicksLeftover);
                    int giveAmplifier = (int) ((float) regenDuration / maxTicksLeftover * qLevel);
                    regenAmplifier = MathHelper.clamp(regenEffect.getAmplifier(), giveAmplifier, maxLevel);
                }
                if (regenAmplifier >= 0 && regenDuration > 0) {
                    if (regenEffect != null && regenAmplifier > regenEffect.getAmplifier()) {
                        Vector3d sparksPos = new Vector3d(entityToHeal.getX(), entityToHeal.getY(0.5), entityToHeal.getZ());
                        HamonUtil.emitHamonSparkParticles(world, null, sparksPos, 0.1f);
                    }
                    
                    hamon.regenImpliedDuration = OptionalInt.of(regenDuration);
                    regenDuration = updateRegenEffect(entityToHeal, regenDuration, regenAmplifier, Effects.REGENERATION);
                    entityToHeal.addEffect(new EffectInstance(Effects.REGENERATION, regenDuration, regenAmplifier, false, false, true));
                }
                
                int reduceEffectTime = 3200 / maxTicksLeftover; // <
                int durationDecrease = ticksInc; // >
                
                boolean healedBleeding = reduceHarmfulEffect(entityToHeal, ModStatusEffects.BLEEDING.get(), ticksHeld, durationDecrease, reduceEffectTime);
                boolean hadHarmfulEffects = healedBleeding;
                if (hamon.isSkillLearned(ModHamonSkills.EXPEL_VENOM.get())) {
                    for (Effect venomEffect : VENOM_EFFECTS) {
                        boolean healedVenom = reduceHarmfulEffect(entityToHeal, venomEffect, ticksHeld, durationDecrease, reduceEffectTime);
                        hadHarmfulEffects |= healedVenom;
                    }
                }
                if (hamon.isSkillLearned(ModHamonSkills.PLANTS_GROWTH.get()) && user instanceof PlayerEntity && target.getType() == TargetType.BLOCK) {
                    Direction face = target.getType() == TargetType.BLOCK ? target.getFace() : Direction.UP;
                    bonemealEffect(user.level, (PlayerEntity) user, target.getBlockPos(), face);
                }
                
                boolean hitLimit = regenEffect != null && regenEffect.getAmplifier() == maxLevel
                        && hamon.regenImpliedDuration.isPresent() && hamon.regenImpliedDuration.getAsInt() >= maxTicksLeftover;
                float points = Math.min(tickEnergyCost, power.getEnergy());
                if (points > 0) {
                    if (hitLimit) {
                        points *= Math.max(HamonData.ENERGY_TICK_DOWN_AMOUNT / tickEnergyCost, 2);
                    }
                    if (entityToHeal.getHealth() < entityToHeal.getMaxHealth() || hadHarmfulEffects) {
                        points *= 4;
                    }
                    if (entityToHeal != user) {
                        points *= 2;
                    }
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, points);
                }
            }
            else {
                HamonSparksLoopSound.playSparkSound(user, user.position(), 1.0F);
                CustomParticlesHelper.createHamonSparkParticles(user instanceof PlayerEntity ? (PlayerEntity) user : null, 
                        user.getRandomX(1), user.getRandomY(), user.getRandomZ(1), 1);
            }
        }
    }
    
    private boolean reduceHarmfulEffect(LivingEntity entity, Effect effect,
            int ticksHeld, int durationDecrease, int reduceEffectTime) {
        EffectInstance effectInstance = entity.getEffect(effect);
        if (effectInstance != null) {
            int amplifier = effectInstance.getAmplifier();
            if (amplifier > 0 && ticksHeld > 0 && ticksHeld % reduceEffectTime == 0) {
                --amplifier;
            }
            
            if (amplifier == 0) {
                durationDecrease *= 2;
            }
            int duration = effectInstance.getDuration() - durationDecrease;
            if (duration > 0) {
                duration = updateKnownEffect(entity, duration, amplifier, effect);
            }
            
            if (duration <= 0) {
                entity.removeEffect(effect);
            }
            else {
                MCUtil.reduceEffect(entity, effect, 
                        effectInstance.getDuration() - duration, 
                        effectInstance.getAmplifier() - amplifier);
            }
            
            return true;
        }
        return false;
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, INonStandPower power, 
            int ticksHeld, boolean willFire) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            hamon.regenImpliedDuration = OptionalInt.empty();
        }
    }
    
//    @Override
//    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
//        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
//        float cost = getEnergyCost(power, target);
//        float hamonEfficiency = hamon.getActionEfficiency(cost, true);
//        float hamonControl = hamon.getHamonControlLevelRatio();
//        
//        if (!world.isClientSide() && hamonEfficiency > 0) {
//            Entity targetEntity = target.getType() == TargetType.ENTITY && hamon.isSkillLearned(ModHamonSkills.HEALING_TOUCH.get()) ? target.getEntity() : null;
//            LivingEntity targetLiving = targetEntity instanceof LivingEntity ? (LivingEntity) targetEntity : null;
//            LivingEntity entityToHeal = targetEntity != null && canBeHealed(targetLiving, user) ? targetLiving : user;
//            int regenDuration = (int) ((50F + hamonEfficiency * 50F) * (1 + hamonControl));
//            int regenLvl = MathHelper.clamp((int) ((hamonControl - 0.0001F) * 3 + (hamonEfficiency - 0.75F) * 4 - 1), 0, 2);
////            if (entityToHeal.getHealth() < entityToHeal.getMaxHealth()) {
//                addPointsForAction(power, hamon, HamonStat.CONTROL, cost, hamonEfficiency);
////            }
//            
//            updateRegenEffect(entityToHeal, regenDuration, regenLvl);
//            if (hamon.isSkillLearned(ModHamonSkills.EXPEL_VENOM.get())) {
//                if (VENOM_EFFECTS == null) {
//                    lazyInitVenomEffects();
//                }
//                for (Effect effect : VENOM_EFFECTS) {
//                    entityToHeal.removeEffect(effect);
//                }
//            }
//            if (hamon.isSkillLearned(ModHamonSkills.PLANTS_GROWTH.get()) && user instanceof PlayerEntity && target.getType() == TargetType.BLOCK) {
//                Direction face = target.getType() == TargetType.BLOCK ? target.getFace() : Direction.UP;
//                bonemealEffect(user.level, (PlayerEntity) user, target.getBlockPos(), face);
//            }
//            Vector3d sparksPos = new Vector3d(entityToHeal.getX(), entityToHeal.getY(0.5), entityToHeal.getZ());
//            HamonUtil.emitHamonSparkParticles(world, null, sparksPos, Math.max(0.5F * hamonControl * hamonEfficiency, 0.1F));
//        }
//    }
    
    private static List<Effect> VENOM_EFFECTS;
    public static void initVenomEffects() {
        VENOM_EFFECTS = VENOM_EFFECTS_INIT.stream()
                .map(id -> ForgeRegistries.POTIONS.containsKey(id) ? ForgeRegistries.POTIONS.getValue(id) : null)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }
    
    public static int updateRegenEffect(LivingEntity entity, int duration, int level, Effect effect) {
        return updateEffect(entity, duration, level, effect, 50);
    }

    public static int updateKnownEffect(LivingEntity entity, int duration, int level, Effect effect) {
        if (effect == Effects.REGENERATION || effect == ModStatusEffects.UNDEAD_REGENERATION.get()) {
            return updateEffect(entity, duration, level, effect, 50);
        }
        else if (effect == Effects.POISON) {
            return updateEffect(entity, duration, level, effect, 25);
        }
        else if (effect == Effects.WITHER) {
            return updateEffect(entity, duration, level, effect, 40);
        }
        
        return duration;
    }
    
    // prevents the health regeneration/damage being faster or slower than it should be when giving an effect frequently
    public static int updateEffect(LivingEntity entity, int duration, int level, Effect effect, int level0Gap) {
        EffectInstance oldEffect = entity.getEffect(effect);
        if (oldEffect != null && level < MathHelper.log2(level0Gap)) {
            int effectGap = level0Gap >> oldEffect.getAmplifier();
            if (effectGap > 0) {
                int oldEffectAppliesIn = oldEffect.getDuration() % (level0Gap >> oldEffect.getAmplifier());
                int newEffectGap = level0Gap >> level;
                int newEffectAppliesIn = newEffectGap > 0 ? duration % newEffectGap : 0;
                
                if (newEffectAppliesIn < oldEffectAppliesIn) {
                    int newDuration = duration + (oldEffectAppliesIn - newEffectAppliesIn);
                    while (newDuration > duration) {
                        newDuration -= newEffectGap;
                    }
                    if (newDuration > 0) {
                        duration = newDuration;
                    }
                }
                else {
                    duration -= (newEffectAppliesIn - oldEffectAppliesIn);
                }
            }
        }
        return duration;
    }
    
    private boolean canBeHealed(LivingEntity targetEntity, LivingEntity user) {
        return !(JojoModUtil.isUndeadOrVampiric(targetEntity) ||
                targetEntity instanceof GolemEntity ||
                targetEntity instanceof ArmorStandEntity);
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(INonStandPower power, String key) {
        if (power.getUser() != null && JojoModUtil.useShiftVar(power.getUser())) {
            ActionTarget target = ActionsOverlayGui.getInstance().getMouseTarget();
            if (power.getTypeSpecificData(ModPowers.HAMON.get())
                    .map(hamon -> hamon.isSkillLearned(ModHamonSkills.HEALING_TOUCH.get())).orElse(false)
                    && target.getType() == TargetType.ENTITY) {
                Entity targetEntity = target.getEntity();
                if (targetEntity instanceof LivingEntity && canBeHealed((LivingEntity) targetEntity, power.getUser())) {
                    key += "_touch";
                    return new TranslationTextComponent(key, targetEntity.getName());
                }
            }
        }
        return super.getTranslatedName(power, key);
    }
    
    
    
    public static boolean bonemealEffect(World world, PlayerEntity applyingPlayer, BlockPos pos, Direction face) {
        if (BoneMealItem.applyBonemeal(ItemStack.EMPTY, world, pos, applyingPlayer)) {
            if (!world.isClientSide()) {
                world.levelEvent(2005, pos, 0);
            }
            return true;
        } else {
            BlockPos posOffset = pos.relative(face);
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isFaceSturdy(world, pos, face) && BoneMealItem.growWaterPlant(new ItemStack(null), world, posOffset, face)) {
                if (!world.isClientSide()) {
                    world.levelEvent(2005, posOffset, 0);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    // f this
//    public static boolean gradualBonemealEffect(ServerWorld world, PlayerEntity applyingPlayer, BlockPos pos, Direction clickedSide) {
//        BlockState clickedBlock = world.getBlockState(pos);
//        int hook = ForgeEventFactory.onApplyBonemeal(applyingPlayer, world, pos, clickedBlock, ItemStack.EMPTY);
//        if (hook != 0) return hook > 0;
//        Random random = world.random;
//        
//        if (clickedBlock.getBlock() instanceof IGrowable) {
//            IGrowable igrowable = (IGrowable) clickedBlock.getBlock();
//            if (igrowable.isValidBonemealTarget(world, pos, clickedBlock, world.isClientSide)) {
//                if (igrowable.isBonemealSuccess(world, world.random, pos, clickedBlock)) {
//                    if (igrowable instanceof GrassBlock) {
//                        // TODO just one plant block & particles
//                        BlockPos blockpos = pos.above();
//                        BlockState grassBlock = Blocks.GRASS.defaultBlockState();
//
//                        label48:
//                            for (int i = 0; i < 128; ++i) {
//                                BlockPos blockpos1 = blockpos;
//
//                                for(int j = 0; j < i / 16; ++j) {
//                                    blockpos1 = blockpos1.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
//                                    if (!world.getBlockState(blockpos1.below()).is(clickedBlock.getBlock()) || world.getBlockState(blockpos1).isCollisionShapeFullBlock(world, blockpos1)) {
//                                        continue label48;
//                                    }
//                                }
//
//                                BlockState blockstate2 = world.getBlockState(blockpos1);
//                                if (blockstate2.is(grassBlock.getBlock()) && random.nextInt(10) == 0) {
//                                    ((IGrowable) grassBlock.getBlock()).performBonemeal(world, random, blockpos1, blockstate2);
//                                }
//
//                                if (blockstate2.isAir()) {
//                                    BlockState blockstate1;
//                                    if (random.nextInt(8) == 0) {
//                                        List<ConfiguredFeature<?, ?>> list = world.getBiome(blockpos1).getGenerationSettings().getFlowerFeatures();
//                                        if (list.isEmpty()) {
//                                            continue;
//                                        }
//
//                                        ConfiguredFeature<?, ?> configuredfeature = list.get(0);
//                                        FlowersFeature flowersfeature = (FlowersFeature)configuredfeature.feature;
//                                        blockstate1 = flowersfeature.getRandomFlower(random, blockpos1, configuredfeature.config());
//                                    } else {
//                                        blockstate1 = grassBlock;
//                                    }
//
//                                    if (blockstate1.canSurvive(world, blockpos1)) {
//                                        world.setBlock(blockpos1, blockstate1, 3);
//                                    }
//                                }
//                            }
//                    }
//                    else {
//                        igrowable.performBonemeal(world, world.random, pos, clickedBlock);
//                    }
//                }
//                
//                world.levelEvent(2005, pos, 0);
//                return true;
//            }
//        }
//
//
//        
//        BlockPos posOffset = pos.relative(clickedSide);
//        BlockState blockState = world.getBlockState(pos);
//        if (blockState.isFaceSturdy(world, pos, clickedSide)) {
//            if (world.getBlockState(posOffset).is(Blocks.WATER) && world.getFluidState(posOffset).getAmount() == 8) {
//                label80:
//                    for (int i = 0; i < 128; ++i) {
//                        BlockPos blockpos = posOffset;
//                        BlockState blockstate = Blocks.SEAGRASS.defaultBlockState();
//
//                        for(int j = 0; j < i / 16; ++j) {
//                            blockpos = blockpos.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
//                            if (world.getBlockState(blockpos).isCollisionShapeFullBlock(world, blockpos)) {
//                                continue label80;
//                            }
//                        }
//
//                        Optional<RegistryKey<Biome>> optional = world.getBiomeName(blockpos);
//                        if (Objects.equals(optional, Optional.of(Biomes.WARM_OCEAN)) || Objects.equals(optional, Optional.of(Biomes.DEEP_WARM_OCEAN))) {
//                            if (i == 0 && clickedSide != null && clickedSide.getAxis().isHorizontal()) {
//                                blockstate = BlockTags.WALL_CORALS.getRandomElement(world.random).defaultBlockState().setValue(DeadCoralWallFanBlock.FACING, clickedSide);
//                            } else if (random.nextInt(4) == 0) {
//                                blockstate = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random).defaultBlockState();
//                            }
//                        }
//
//                        if (blockstate.getBlock().is(BlockTags.WALL_CORALS)) {
//                            for(int k = 0; !blockstate.canSurvive(world, blockpos) && k < 4; ++k) {
//                                blockstate = blockstate.setValue(DeadCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
//                            }
//                        }
//
//                        if (blockstate.canSurvive(world, blockpos)) {
//                            BlockState blockstate1 = world.getBlockState(blockpos);
//                            if (blockstate1.is(Blocks.WATER) && world.getFluidState(blockpos).getAmount() == 8) {
//                                world.setBlock(blockpos, blockstate, 3);
//                            } else if (blockstate1.is(Blocks.SEAGRASS) && random.nextInt(10) == 0) {
//                                ((IGrowable)Blocks.SEAGRASS).performBonemeal((ServerWorld)world, random, blockpos, blockstate1);
//                            }
//                        }
//                    }
//
//                world.levelEvent(2005, posOffset, 0);
//                return true;
//            }
//        }
//        
//        return false;
//    }
}
