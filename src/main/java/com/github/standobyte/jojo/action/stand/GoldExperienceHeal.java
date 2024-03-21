package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.HamonHealing;
import com.github.standobyte.jojo.action.stand.effect.GEHealingEffect;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class GoldExperienceHeal extends StandEntityAction {
    
    public GoldExperienceHeal(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        return canHeal(user, user, false, MAX_REGEN_LVL);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            spendAndHeal(world, user, user, userPower, standEntity);
        }
    }
    

    public static final int MAX_REGEN_LVL = 3;
    
    public static boolean isLiving(LivingEntity entity) {
        return !(!(entity instanceof PlayerEntity) && JojoModUtil.isUndead(entity) ||
                entity instanceof GolemEntity ||
                entity instanceof ArmorStandEntity);
    }
    
    public static ActionConditionResult canHeal(LivingEntity entity, LivingEntity userGE, 
            boolean tissueItem, int effectMax) {
        if (entity != null) {
            if (!isLiving(entity)) {
                return conditionMessage("ge_heal_non_living");
            }
            if (StandUtil.getStandUser(entity) != entity) {
                return conditionMessage("ge_heal_stand");
            }
            
            if (!tissueItem) {
                int arrows = entity.getArrowCount();
                if (arrows > 0) {
                    return ActionConditionResult.POSITIVE;
                }
                int knives = getKnivesCount(entity);
                if (knives > 0) {
                    return ActionConditionResult.POSITIVE;
                }
                
                ItemStack offHandItem = userGE.getOffhandItem();
                if (offHandItem.isEmpty()) {
                    return conditionMessage("ge_lifeform_material_only_item");
                }
                if (!GoldExperienceCreateLifeform.canGiveLifeTo(offHandItem)) {
                    return conditionMessage("ge_lifeform_material_item");
                }
            }
            
            int currentRegen = MCUtil.getEffectLevel(entity, regenEffectFor(entity));
            if (currentRegen >= effectMax) {
                if (entity == userGE) {
                    return conditionMessage("ge_heal_stronger");
                }
                else {
                    return conditionMessage("ge_heal_stronger.other", entity.getDisplayName());
                }
            }
            
            if (entity.getHealth() >= entity.getMaxHealth()) {
                if (entity == userGE) {
                    return conditionMessage("ge_heal_full_hp");
                }
                else {
                    return conditionMessage("ge_heal_full_hp.other", entity.getDisplayName());
                }
            }
            
            return ActionConditionResult.POSITIVE;
        }
        
        return ActionConditionResult.NEGATIVE;
    }
    
    private static Effect regenEffectFor(LivingEntity entity) {
        if (entity instanceof PlayerEntity && JojoModUtil.isPlayerUndead((PlayerEntity) entity)) {
            return ModStatusEffects.UNDEAD_REGENERATION.get();
        }
        return Effects.REGENERATION;
    }
    
    public static void spendAndHeal(World world, LivingEntity entity, 
            LivingEntity user, IStandPower userPower, StandEntity standEntity) {
        if (entity != null && !world.isClientSide()) {
            boolean stuckProjectile = false;
            int arrows = entity.getArrowCount();
            if (arrows > 0) {
                entity.setArrowCount(arrows - 1);
                stuckProjectile = true;
            }
            else {
                int knives = getKnivesCount(entity);
                if (knives > 0) {
                    entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(data -> data.setKnives(knives - 1));
                    stuckProjectile = true;
                }
            }
            
            if (stuckProjectile) {
                entity.hurt(DamageSource.GENERIC, 0.0001F);
                Effect regen = regenEffectFor(entity);
                int lvl = Math.min(MCUtil.getEffectLevel(entity, regen) + 1, MAX_REGEN_LVL);
                entity.addEffect(new EffectInstance(regen, HamonHealing.updateRegenEffect(entity, 105, lvl, regen), lvl));
                
                MCUtil.playSound(entity.level, null, entity, ModSounds.GOLD_EXPERIENCE_HEAL.get(), 
                        SoundCategory.AMBIENT, 1.0F, 0.95F + entity.getRandom().nextFloat() * 0.1F, StandUtil::playerCanHearStands);
            }
            else {
                ItemStack offHandItem = user.getOffhandItem();
                if (offHandItem.getItem() instanceof BucketItem) {
                    BucketItem bucketType = (BucketItem) offHandItem.getItem();
                    bucketType.checkExtraContent(world, offHandItem, getControlledEntity(user, userPower).blockPosition());
                }
                if (!(user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild)) {
                    offHandItem.shrink(1);
                }
                
                giveGEHealEffect(entity, userPower, 6000);
            }
        }
    }
    
    public static void giveGEHealEffect(LivingEntity entity, IStandPower userPower, int durationMax) {
        Effect regenEffect = regenEffectFor(entity);
        EffectInstance currentRegen = entity.getEffect(regenEffect);
        
        int lvl;
        int duration = durationMax;
        if (currentRegen != null) {
            lvl = currentRegen.getAmplifier() + 1;
            duration = HamonHealing.updateRegenEffect(entity, duration, lvl, regenEffect);
        }
        else {
            lvl = 0;
        }
        
        if (userPower != null) {
            GEHealingEffect healingTracker = userPower.getContinuousEffects()
                    .getOrCreateEffect(ModStandEffects.GE_HEALING.get(), entity);
            healingTracker.fullHpTicks = 0;
            healingTracker.regenLevel = lvl;
            if (healingTracker.tickCount == 0 && currentRegen != null) {
                healingTracker.prevEffect = new EffectInstance(currentRegen);
            }
        }

        entity.hurt(DamageSource.GENERIC, 0.0001F);
        
        EffectInstance newRegen = new EffectInstance(regenEffect, duration, lvl, false, true, true, currentRegen);
        entity.addEffect(newRegen);
        
        MCUtil.playSound(entity.level, null, entity, ModSounds.GOLD_EXPERIENCE_HEAL.get(), 
                SoundCategory.AMBIENT, 1.0F, 0.95F + entity.getRandom().nextFloat() * 0.1F, StandUtil::playerCanHearStands);
    }
    
    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String postfix = getPostfix(power.getUser());
        String key = super.getTranslationKey(power, target);
        return postfix != null ? key + postfix : key;
    }
    
    protected static int getKnivesCount(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return entity.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getKnivesCount).orElse(0);
        }
        return 0;
    }
    
    protected String getPostfix(LivingEntity entityToHeal) {
        int arrows = entityToHeal.getArrowCount();
        if (arrows > 0) {
            return ".arrow";
        }
        
        int knives = getKnivesCount(entityToHeal);
        if (knives > 0) {
            return ".knife";
        }
        
        return null;
    }
    
}
