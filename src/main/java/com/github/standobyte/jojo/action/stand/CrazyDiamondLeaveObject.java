package com.github.standobyte.jojo.action.stand;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Predicate;

import org.apache.logging.log4j.util.TriConsumer;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.ModInteractionUtil;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;

public class CrazyDiamondLeaveObject extends StandEntityActionModifier {
    
    static final Map<Predicate<ItemStack>, TriConsumer<LivingEntity, ItemStack, LivingEntity>> ITEM_ACTION = Util.make(new HashMap<>(), map -> {
        map.put(item -> item.getItem() == Items.CHORUS_FRUIT, (target, item, user) -> chorusFruitTeleport(target, user));
        map.put(item -> item.getItem() == Items.SNOWBALL, (target, item, user) -> target.addEffect(new EffectInstance(ModEffects.FREEZE.get(), 40, 0)));
        map.put(item -> item.getItem() == Items.SNOW, (target, item, user) -> target.addEffect(new EffectInstance(ModEffects.FREEZE.get(), 80, 0)));
        map.put(item -> item.getItem() == Items.SNOW_BLOCK, (target, item, user) -> target.addEffect(new EffectInstance(ModEffects.FREEZE.get(), 120, 1)));
        map.put(item -> item.getItem() == Items.ICE, (target, item, user) -> target.addEffect(new EffectInstance(ModEffects.FREEZE.get(), 200, 1)));
        map.put(item -> item.getItem() == Items.PACKED_ICE, (target, item, user) -> target.addEffect(new EffectInstance(ModEffects.FREEZE.get(), 200, 2)));
        map.put(item -> item.getItem() == Items.BLUE_ICE, (target, item, user) -> target.addEffect(new EffectInstance(ModEffects.FREEZE.get(), 200, 3)));
        map.put(item -> item.getItem() == Items.FIRE_CHARGE, (target, item, user) -> DamageUtil.setOnFire(target, 15, false));
        map.put(item -> item.getItem() == Items.BLAZE_POWDER, (target, item, user) -> DamageUtil.setOnFire(target, 20, false));
        map.put(item -> item.getItem() == Items.BLAZE_ROD, (target, item, user) -> DamageUtil.setOnFire(target, 30, false));
        map.put(item -> item.getItem() == Items.LAVA_BUCKET, (target, item, user) -> DamageUtil.setOnFire(target, 30, false));
        map.put(item -> item.getItem() == Items.EXPERIENCE_BOTTLE, (target, item, user) -> giveXp(target));
        map.put(item -> !PotionUtils.getMobEffects(item).isEmpty(), (target, item, user) -> PotionUtils.getMobEffects(item).forEach(effect -> target.addEffect(effect)));
        // FIXME !!!! (normal heavy) shrinks the stack
//        map.put(item -> item.isEdible(), (target, item, user) -> target.eat(target.level, item));
    });

    public CrazyDiamondLeaveObject(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        // FIXME !!!!! (normal heavy) check the STAND item
        ItemStack item = user.getOffhandItem();
        return ActionConditionResult.noMessage(!item.isEmpty() && canUseItem(item));
    }
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        // FIXME !!!!! (normal heavy) could be input later
        if (!world.isClientSide() && task.getTaskCompletion(1.0F) >= 0.45F) {
            if (task.getTarget().getType() == TargetType.ENTITY) {
                Entity entity = task.getTarget().getEntity();
                if (entity.isAlive() && entity instanceof LivingEntity && !(entity instanceof SkeletonEntity) && !(entity instanceof StandEntity)) {
                    LivingEntity targetEntity = (LivingEntity) entity;
                    ItemStack item = standEntity.getMainHandItem();
                    if (!item.isEmpty()) {
                        ITEM_ACTION.forEach((itemCheck, itemEffect) -> {
                            if (itemCheck.test(item)) {
                                itemEffect.accept(targetEntity, item, userPower.getUser());
                            }
                        });
                        item.shrink(1);
                    }
                }
            }
            // FIXME !!!!! (normal heavy) call this from the heavy attack!
            // FIXME !!!!! (normal heavy) call this more reliably
            returnOffHandItemToUser(standEntity, userPower.getUser());
        }
    }
    
    static boolean canUseItem(ItemStack itemStack) {
        return ITEM_ACTION.keySet().stream().anyMatch(predicate -> predicate.test(itemStack));
    }
    
    private void returnOffHandItemToUser(StandEntity standEntity, LivingEntity user) {
        if (user == null || standEntity.getMainHandItem().isEmpty()) return;
        JojoModUtil.giveItemTo(user, standEntity.getMainHandItem());
    }
    
    @Override
    public TranslationTextComponent getTranslatedName(IStandPower power, String key) {
        if (power.getStandManifestation() instanceof StandEntity) {
            ItemStack item = ((StandEntity) power.getStandManifestation()).getMainHandItem();
            if (!item.isEmpty()) {
                return new TranslationTextComponent(key, item.getDisplayName());
            }
        }
        return super.getTranslatedName(power, key);
    }
    
    

    private static void chorusFruitTeleport(LivingEntity target, LivingEntity user) {
        if (ModInteractionUtil.isEntityEnderman(target)) return;
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
}
