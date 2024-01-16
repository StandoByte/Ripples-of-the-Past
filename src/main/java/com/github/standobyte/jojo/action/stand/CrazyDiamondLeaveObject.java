package com.github.standobyte.jojo.action.stand;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import org.apache.logging.log4j.util.TriConsumer;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.IPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.EyeOfEnderInsideEntity;
import com.github.standobyte.jojo.entity.FireworkInsideEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.ModInteractionUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.EyeOfEnderEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;

public class CrazyDiamondLeaveObject extends StandEntityActionModifier {
    
    static final Map<Predicate<ItemStack>, TriConsumer<LivingEntity, ItemStack, LivingEntity>> ITEM_ACTION = Util.make(new HashMap<>(), map -> {
        map.put(item -> item.getItem() == Items.CHORUS_FRUIT, (target, item, user) ->           chorusFruitTeleport(target, user));
        map.put(item -> item.getItem() == Items.ENDER_EYE, (target, item, user) ->              enderEyeFlight(target, item, user));
        map.put(item -> item.getItem() == Items.FIREWORK_ROCKET, (target, item, user) ->        fireworkFlight(target, item, user));
        map.put(item -> item.getItem() == Items.SNOWBALL, (target, item, user) ->               target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), 40, 0)));
        map.put(item -> item.getItem() == Items.SNOW, (target, item, user) ->                   target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), 80, 0)));
        map.put(item -> item.getItem() == Items.SNOW_BLOCK, (target, item, user) ->             target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), 120, 1)));
        map.put(item -> item.getItem() == Items.ICE, (target, item, user) ->                    target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), 200, 1)));
        map.put(item -> item.getItem() == Items.PACKED_ICE, (target, item, user) ->             target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), 200, 2)));
        map.put(item -> item.getItem() == Items.BLUE_ICE, (target, item, user) ->               target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), 200, 3)));
        map.put(item -> item.getItem() == Items.FIRE_CHARGE, (target, item, user) ->            DamageUtil.setOnFire(target, 5, false));
        map.put(item -> item.getItem() == Items.BLAZE_POWDER, (target, item, user) ->           DamageUtil.setOnFire(target, 10, false));
        map.put(item -> item.getItem() == Items.BLAZE_ROD, (target, item, user) ->              DamageUtil.setOnFire(target, 20, false));
        map.put(item -> item.getItem() == Items.LAVA_BUCKET, (target, item, user) ->            DamageUtil.setOnFire(target, 20, false));
        map.put(item -> item.getItem() == Items.GLOWSTONE_DUST, (target, item, user) ->         target.addEffect(new EffectInstance(Effects.GLOWING, 100)));
        map.put(item -> item.getItem() == Items.SPECTRAL_ARROW, (target, item, user) ->         target.addEffect(new EffectInstance(Effects.GLOWING, 200)));
        map.put(item -> item.getItem() == Items.GLOWSTONE, (target, item, user) ->              target.addEffect(new EffectInstance(Effects.GLOWING, 400)));
        map.put(item -> item.getItem() == Items.EXPERIENCE_BOTTLE, (target, item, user) ->      giveXp(target));
        map.put(item -> item.getItem() == Items.MILK_BUCKET, (target, item, user) ->            target.curePotionEffects(item));
        map.put(item -> !PotionUtils.getMobEffects(item).isEmpty(), (target, item, user) ->     PotionUtils.getMobEffects(item).forEach(effect -> target.addEffect(effect)));
        map.put(item -> item.isEdible(), (target, item, user) ->                                target.eat(target.level, item.copy()));
        map.put(item -> item.getItem() == Items.ENCHANTED_GOLDEN_APPLE, (target, item, user) -> VampirismUtil.onEnchantedGoldenAppleEaten(target));
    });
    static final Map<Predicate<ItemStack>, TriPredicate<LivingEntity, ItemStack, LivingEntity>> ITEM_ACTION_CONDITIONAL = Util.make(new HashMap<>(), map -> {
        map.put(item -> item.getItem() instanceof StandArrowItem, (target, item, user) ->       StandArrowItem.onPiercedByArrow(target, item, target.level, Optional.of(user)));
    });

    public CrazyDiamondLeaveObject(Builder builder) {
        super(builder);
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return ModStandsInit.CRAZY_DIAMOND_HEAL.get().isUnlocked(power);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive()) {
            StandEntity standEntity = (StandEntity) power.getStandManifestation();
            ItemStack item = standEntity.getMainHandItem();
            return ActionConditionResult.noMessage(!item.isEmpty() && canUseItem(item));
        }
        return ActionConditionResult.NEGATIVE;
    }
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        boolean triggerEffect = task.getTicksLeft() <= 1;
        if (task.getAdditionalData().isEmpty(TriggeredFlag.class) && task.getTarget().getType() == TargetType.ENTITY) {
            Entity entity = task.getTarget().getEntity();
            if (entity.isAlive() && entity instanceof LivingEntity && !(entity instanceof SkeletonEntity) && !(entity instanceof StandEntity)) {
                if (world.isClientSide()) {
                    if (ClientUtil.canSeeStands()) {
                        CrazyDiamondHeal.addParticlesAround(entity);
                    }
                    if (task.getTick() == 0 && ClientUtil.canHearStands()) {
                        world.playLocalSound(entity.getX(), entity.getY(0.5), entity.getZ(), ModSounds.CRAZY_DIAMOND_FIX_STARTED.get(), 
                                standEntity.getSoundSource(), 1.0F, 1.0F, false);
                    }
                }
                else if (triggerEffect) {
                    LivingEntity targetEntity = (LivingEntity) entity;
                    
                    ItemStack item = standEntity.getMainHandItem();
                    boolean itemUsed = false;
                    if (!item.isEmpty()) {
                        boolean itemAction1 = ITEM_ACTION.entrySet().stream().filter(entry -> {
                            boolean triggered = false;
                            Predicate<ItemStack> itemCheck = entry.getKey();
                            TriConsumer<LivingEntity, ItemStack, LivingEntity> itemEffect = entry.getValue();
                            if (!world.isClientSide() && itemCheck.test(item)) {
                                itemEffect.accept(targetEntity, item, userPower.getUser());
                                triggered = true;
                            }
                            return triggered;
                        }).findAny().isPresent();
                        
                        boolean itemAction2 = ITEM_ACTION_CONDITIONAL.entrySet().stream().filter(entry -> {
                            boolean triggered = false;
                            Predicate<ItemStack> itemCheck = entry.getKey();
                            TriPredicate<LivingEntity, ItemStack, LivingEntity> itemEffect = entry.getValue();
                            if (!world.isClientSide() && itemCheck.test(item) && itemEffect.test(targetEntity, item, userPower.getUser())) {
                                triggered = true;
                            }
                            return triggered;
                        }).findAny().isPresent();
                        
                        itemUsed = itemAction1 || itemAction2;
                        if (itemUsed) {
                            item.shrink(1);
                        }
                    }
                    
                    if (itemUsed) {
                        IPunch punch = standEntity.getLastPunch();
                        float damageDealt = punch.getType() == TargetType.ENTITY ? ((StandEntityPunch) punch).getDamageDealtToLiving() : 0;
                        targetEntity.setHealth(targetEntity.getHealth() + damageDealt * 0.5F);
                    }
                }
            }
            if (triggerEffect) {
                task.getAdditionalData().push(TriggeredFlag.class, new TriggeredFlag());
            }
        }
    }
    
    static boolean canUseItem(ItemStack itemStack) {
        return ITEM_ACTION.keySet().stream().anyMatch(predicate -> predicate.test(itemStack))
                || ITEM_ACTION_CONDITIONAL.keySet().stream().anyMatch(predicate -> predicate.test(itemStack));
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        if (power.getStandManifestation() instanceof StandEntity) {
            ItemStack item = ((StandEntity) power.getStandManifestation()).getMainHandItem();
            if (!item.isEmpty()) {
                return new TranslationTextComponent(key, item.getDisplayName());
            }
        }
        return super.getTranslatedName(power, key);
    }
    
    

    private static void chorusFruitTeleport(LivingEntity target, LivingEntity user) {
        if (ModInteractionUtil.isEntityEnderman(target)) {
            target.heal(6);
            return;
        }
        double xPrev = target.getX();
        double yPrev = target.getY();
        double zPrev = target.getZ();

        for(int tpTry = 0; tpTry < 16; ++tpTry) {
            Random random = target.getRandom();
//            double x = entity.getX() + (random.nextDouble() - 0.5D) * 16.0D;
//            double y = MathHelper.clamp(entity.getY() + (double)(random.nextInt(16) - 8), 0.0D, (double)(entity.level.getHeight() - 1));
//            double z = entity.getZ() + (random.nextDouble() - 0.5D) * 16.0D;
            double x;
            double y = MathHelper.clamp(target.getY() + (double)(random.nextInt(16) - 8), 0.0D, (double)(target.level.getHeight() - 1));
            double z;
            if (user != null) {
                Vector3d middlePos = target.position().add(user.getLookAngle().scale(12));
                x = middlePos.x + (random.nextDouble() - 0.5) * 8.0;
                z = middlePos.z + (random.nextDouble() - 0.5) * 8.0;
            }
            else {
                x = xPrev + (random.nextDouble() + 1.0) * (random.nextBoolean() ? 1 : -1) * 8.0;
                z = zPrev + (random.nextDouble() + 1.0) * (random.nextBoolean() ? 1 : -1) * 8.0;
            }
            if (target.isPassenger()) {
                target.stopRiding();
            }

            EntityTeleportEvent.ChorusFruit event = ForgeEventFactory.onChorusFruitTeleport(target, x, y, z);
            if (!event.isCanceled() && target.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                SoundEvent soundevent = target instanceof FoxEntity ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                target.yRot = random.nextFloat() * 360F;
                target.yRotO = target.yRot;
                target.level.playSound((PlayerEntity)null, xPrev, yPrev, zPrev, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                target.playSound(soundevent, 1.0F, 1.0F);
                break;
            }
        }
    }
    
    private static void giveXp(LivingEntity entity) {
        entity.level.levelEvent(2002, entity.blockPosition().above(), PotionUtils.getColor(Potions.WATER));
        int xp = 3 + entity.level.random.nextInt(5) + entity.level.random.nextInt(5);
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            Entry<EquipmentSlotType, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged);
            if (entry != null) {
                ItemStack itemstack = entry.getValue();
                if (!itemstack.isEmpty() && itemstack.isDamaged()) {
                    int i = Math.min((int)(xp * itemstack.getXpRepairRatio()), itemstack.getDamageValue());
                    xp -= i / 2;
                    itemstack.setDamageValue(itemstack.getDamageValue() - i);
                }
            }

            if (xp > 0) {
                player.giveExperiencePoints(xp);
            }
        }
        else {
            while (xp > 0) {
                int xpThisOrb = ExperienceOrbEntity.getExperienceValue(xp);
                xp -= xpThisOrb;
                entity.level.addFreshEntity(new ExperienceOrbEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), xpThisOrb));
            }
        }
    }
    
    private static void enderEyeFlight(LivingEntity entity, ItemStack item, LivingEntity user) {
        if (!entity.level.isClientSide()) {
            ServerWorld world = (ServerWorld) entity.level;
            BlockPos strongholdPos = world.getChunkSource().getGenerator().findNearestMapFeature(world, Structure.STRONGHOLD, entity.blockPosition(), 100, false);
            if (strongholdPos != null) {
                EyeOfEnderEntity eyeOfEnder = new EyeOfEnderInsideEntity(entity.level, entity);

                eyeOfEnder.setItem(item);
                eyeOfEnder.signalTo(strongholdPos);

                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDER_EYE_LAUNCH, 
                        SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
                world.levelEvent(null, 1003, entity.blockPosition(), 0);

                world.addFreshEntity(eyeOfEnder);
                
                if (entity instanceof ServerPlayerEntity) {
                    CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayerEntity) entity, strongholdPos);
                }
                if (user instanceof ServerPlayerEntity) {
                    CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayerEntity) user, strongholdPos);
                }
            }
        }
    }
    
    private static void fireworkFlight(LivingEntity entity, ItemStack item, LivingEntity user) {
        FireworkRocketEntity firework = new FireworkInsideEntity(entity.level, item, entity);
        entity.level.addFreshEntity(firework);
    }
}
