package com.github.standobyte.jojo.potion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandArrowHandler;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectType;

public class StandVirusEffect extends StatusEffect implements IApplicableEffect {
    
    public StandVirusEffect(int liquidColor) {
        super(EffectType.HARMFUL, liquidColor);
    }
    
    @Override
    public boolean isApplicable(LivingEntity entity) {
        return !StandUtil.isEntityStandUser(entity) && 
                (entity instanceof PlayerEntity || mobMayGetStand(entity));
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide()) {
            float damage = baseDamage(amplifier);
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                
                boolean hasXpLevel = player.abilities.instabuild || player.experienceLevel > 0;
                boolean stopEffect = false;
                if (hasXpLevel) {
                    stopEffect = IStandPower.getStandPowerOptional(player).map(power -> {
                        StandArrowHandler handler = power.getStandArrowHandler();
                        ItemStack arrowPiercedBy = handler.getStandArrowItem();
                        return handler.decXpLevelsTakenByArrow(player) >= handler.getStandXpLevelsRequirement(player.level.isClientSide(), arrowPiercedBy);
                    }).orElse(false);
                }

                if (stopEffect) {
                    entity.removeEffect(this);
                }
                else {
                    player.giveExperienceLevels(-1);
                    if (hasXpLevel) {
                        damage /= 10;
                        if (damage > entity.getHealth()) {
                            damage = 0.001F;
                        }
                    }
                    DamageUtil.hurtThroughInvulTicks(entity, DamageUtil.STAND_VIRUS, damage);
                }
            }
            
            else if (entity.getHealth() > damage) {
                DamageUtil.hurtThroughInvulTicks(entity, DamageUtil.STAND_VIRUS, damage);
            }
            else {
                entity.removeEffect(this);
            }
        }
    }
    
    private static float baseDamage(int amplifier) {
        return 1.5F + amplifier * 2F;
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level.isClientSide() && entity.isAlive()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                IStandPower.getStandPowerOptional(player).ifPresent(
                        power -> {
                            StandType<?> stand = power.getStandArrowHandler().getStandToGive();
                            power.getStandArrowHandler().clearStandToGive();
                            if (stand == null) {
                                stand = StandUtil.randomStand(player, player.getRandom());
                            }
                            if (stand != null) {
                                StandArrowItem.giveStandFromArrow(player, power, stand);
                            }
                        });
            }
            else {
                Optional<MobStandGiver> randomStandGiver = getRandomStandGiver(entity);
                if (randomStandGiver.isPresent()) {
                    randomStandGiver.get().giveStandFromVirus(entity, amplifier);
                }
                else {
                    DamageUtil.hurtThroughInvulTicks(entity, DamageUtil.STAND_VIRUS, baseDamage(amplifier));
                }
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
    
    
    public static final int MAX_VIRUS_INHIBITION = 3;
    
    public static int getEffectLevelToApply(int inhibition) {
        return Math.max(MAX_VIRUS_INHIBITION - inhibition, 0);
    }
    
    public static int getEffectDurationToApply(PlayerEntity player) {
        return IStandPower.getStandPowerOptional(player).map(power -> {
            StandArrowHandler handler = power.getStandArrowHandler();
            return (handler.getStandXpLevelsRequirement(player.level.isClientSide(), ItemStack.EMPTY) + 1) * 20;
        }).orElse(0);
    }
    
    
    
    private static final List<MobStandGiver> MOB_STAND_GIVER = new ArrayList<>();
    
    public static Optional<MobStandGiver> getRandomStandGiver(LivingEntity entity) {
        return MOB_STAND_GIVER.stream()
                .filter(m -> m.entityMatches(entity))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    if (list.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(list.get(entity.getRandom().nextInt(list.size())));
                }));
    }
    
    public static void addMobStandGiver(MobStandGiver mobStandGiver) {
        MOB_STAND_GIVER.add(mobStandGiver);
    }
    
    public static boolean mobMayGetStand(LivingEntity entity) {
        return MOB_STAND_GIVER.stream().anyMatch(standGiver -> standGiver.entityMatches(entity));
    }
    
    public static class MobStandGiver {
        protected Supplier<? extends EntityType<?>> entityType;
        protected List<Supplier<? extends StandType<?>>> stands = new ArrayList<>();
        
        @SafeVarargs
        public MobStandGiver(Supplier<? extends EntityType<?>> entityType, 
                Supplier<? extends StandType<?>> stand, Supplier<? extends StandType<?>>... moreStands) {
            this.entityType = entityType;
            this.stands.add(stand);
            Collections.addAll(this.stands, moreStands);
        }
        
        public void addStand(Supplier<StandType<?>> stand) {
            stands.add(stand);
        }
        
        public boolean entityMatches(LivingEntity entity) {
            return entity.getType() == entityType.get();
        }
        
        protected float getSurviveChance(float virusEffectLvl) {
            return 1 - 0.15f * virusEffectLvl;
        }
        
        public boolean giveStand(LivingEntity entity) {
            return IStandPower.getStandPowerOptional(entity).map(standPower -> {
                StandType<?> stand = stands.get(entity.getRandom().nextInt(stands.size())).get();
                return StandArrowItem.giveStandFromArrow(entity, standPower, stand);
            }).orElse(false);
        }
        
        public void giveStandFromVirus(LivingEntity entity, int virusEffectLvl) {
            boolean gaveStand = false;
            if (entity.getRandom().nextFloat() <= getSurviveChance(virusEffectLvl)) {
                gaveStand = giveStand(entity);
            }
            if (!gaveStand) {
                DamageUtil.hurtThroughInvulTicks(entity, DamageUtil.STAND_VIRUS, baseDamage(virusEffectLvl));
            }
        }
    }
}
