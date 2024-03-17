package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.HamonHealing;
import com.github.standobyte.jojo.action.stand.effect.GEHealingEffect;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
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
        if (user.isDeadOrDying()) {
            return ActionConditionResult.NEGATIVE;
        }
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
    
    public static ActionConditionResult canHeal(LivingEntity entity, LivingEntity userGE, 
            boolean tissueItem, int effectMax) {
        if (entity != null) {
            if (JojoModUtil.isUndead(entity)) {
                return conditionMessage("ge_heal_undead");
            }
            if (StandUtil.getStandUser(entity) != entity) {
                return conditionMessage("ge_heal_stand");
            }
            
            if (entity.isDeadOrDying()) {
                boolean canResurrect = !JojoModUtil.isDyingBody(entity) && !JojoModUtil.isUndead(entity);
                return canResurrect ? ActionConditionResult.POSITIVE : conditionMessage("resurrect_dead");
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
            
            int currentRegen = MCUtil.getEffectLevel(entity, Effects.REGENERATION);
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
    
    public static void spendAndHeal(World world, LivingEntity entity, 
            LivingEntity user, IStandPower userPower, StandEntity standEntity) {
        if (entity != null && !world.isClientSide()) {
            if (entity.isDeadOrDying()) {
                boolean resurrect = entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(data -> {
                    if (data.soulEntity != null && data.soulEntity.isAlive()) {
                        int timeLeft = data.soulEntity.lifeSpan - data.soulEntity.tickCount;
                        return timeLeft <= 20 || entity.getRandom().nextFloat() <= 0.2F;
                    }
                    return false;
                }).orElse(false);
                
                if (resurrect) {
                    entity.setHealth(entity.getMaxHealth());
                    MCUtil.onEntityResurrect(entity);
                    entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(data -> data.setDyingBodyTimer(48000));
                }
                playHealSound(entity);
                return;
            }
            
            
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
                if (JojoModUtil.isDyingBody(entity)) {
                    entity.setHealth(entity.getHealth() + 2.0F);
                }
                else {
                    entity.hurt(DamageSource.GENERIC, 0.0001F);
                    int lvl = Math.min(MCUtil.getEffectLevel(entity, Effects.REGENERATION) + 1, MAX_REGEN_LVL);
                    entity.addEffect(new EffectInstance(Effects.REGENERATION, 
                            HamonHealing.updateRegenEffect(entity, 105, lvl), lvl));
                }
                playHealSound(entity);
                return;
            }
            
            
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
    
    public static void giveGEHealEffect(LivingEntity entity, IStandPower userPower, int durationMax) {
        playHealSound(entity);
        
        
        if (JojoModUtil.isDyingBody(entity)) {
            entity.setHealth(entity.getHealth() + 2.0F);
            return;
        }
        
        
        EffectInstance currentRegen = entity.getEffect(Effects.REGENERATION);
        
        int lvl;
        int duration = durationMax;
        if (currentRegen != null) {
            lvl = currentRegen.getAmplifier() + 1;
            duration = HamonHealing.updateRegenEffect(entity, duration, lvl);
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
        
        EffectInstance newRegen = new EffectInstance(Effects.REGENERATION, duration, lvl, false, true, true, currentRegen);
        entity.addEffect(newRegen);
        
    }
    
    public static void playHealSound(LivingEntity entity) {
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
        if (entityToHeal.isDeadOrDying()) {
            return ".dying";
        }
        
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
