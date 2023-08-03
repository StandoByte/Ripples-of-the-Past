package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.init.ModCustomStats;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class VampirismBloodDrain extends VampirismAction {

    public VampirismBloodDrain(NonStandAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        Entity entityTarget = target.getEntity();
        if (entityTarget instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) entityTarget;
            if (!JojoModUtil.canBleed(livingTarget) || JojoModUtil.isUndead(livingTarget)) {
                return conditionMessage("blood");
//                return livingTarget.tickCount > 20 ? conditionMessageContinueHold("blood") : ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
            }
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide() && target.getEntity() instanceof LivingEntity) {
                LivingEntity targetEntity = (LivingEntity) target.getEntity();
                if (!targetEntity.isDeadOrDying()) {
                    float bloodAndHealModifier = GeneralUtil.getOrLast(
                            JojoModConfig.getCommonConfigInstance(false).bloodDrainMultiplier.get(), 
                            world.getDifficulty().getId()).floatValue();
                    boolean isHuman = false;
                    if (targetEntity instanceof PlayerEntity) {
                        bloodAndHealModifier *= 7.5F;
                        isHuman = true;
                    }
                    else if (targetEntity instanceof INPC || targetEntity instanceof AbstractIllagerEntity) {
                        bloodAndHealModifier *= 5F;
                        isHuman = true;
                    }
                    if (INonStandPower.getNonStandPowerOptional(targetEntity).map(
                            p -> p.getType() == ModPowers.HAMON.get()).orElse(false)) {
                        bloodAndHealModifier *= 4F;
                    }
                    EffectInstance freeze = targetEntity.getEffect(ModStatusEffects.FREEZE.get());
                    if (freeze != null) {
                        bloodAndHealModifier *= 1 - Math.min((freeze.getAmplifier() + 1) * 0.2F, 1);
                    }
                    power.addEnergy(bloodAndHealModifier);
                    if (drainBlood(user, targetEntity, 4)) {
                        if (power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).map(
                                vampirism -> vampirism.isBeingCured() && vampirism.getCuringStage() >= 3).orElse(false)) {
                            user.hurt(new DamageSource("curedVampireBlood"), Math.min(bloodAndHealModifier * 0.5F, user.getHealth() - 1));
                        }
                        else {
                            float healed = user.getHealth();
                            user.heal(bloodAndHealModifier * 0.5F);
                            healed = user.getHealth() - healed;
                            if (healed > 0) {
                                power.addEnergy(healed * VampirismUtil.healCost(world));
                            }
                        }
                        if (targetEntity.isDeadOrDying()) {
                            boolean zombieCreated = HungryZombieEntity.createZombie((ServerWorld) world, user, targetEntity, false);
                            if (user instanceof ServerPlayerEntity) {
                                ServerPlayerEntity player = (ServerPlayerEntity) user;
                                player.awardStat(isHuman ? ModCustomStats.VAMPIRE_PEOPLE_DRAINED : ModCustomStats.VAMPIRE_ANIMALS_DRAINED);
                                if (zombieCreated) {
                                    player.awardStat(ModCustomStats.VAMPIRE_ZOMBIES_CREATED);
                                }
                                ModCriteriaTriggers.VAMPIRE_PEOPLE_DRAINED.get().trigger(player, 
                                        player.getStats().getValue(Stats.CUSTOM.get(ModCustomStats.VAMPIRE_PEOPLE_DRAINED)), 
                                        player.getStats().getValue(Stats.CUSTOM.get(ModCustomStats.VAMPIRE_ZOMBIES_CREATED)));
                            }
                        }
                    }
                }
            }
        }
    }

    private static final Effect[] BLOOD_DRAIN_EFFECTS = {
            Effects.MOVEMENT_SLOWDOWN,
            Effects.DIG_SLOWDOWN,
            Effects.WEAKNESS,
            Effects.CONFUSION
    };
    public static boolean drainBlood(LivingEntity attacker, LivingEntity target, float bloodDrainDamage) {
        boolean hurt = target.hurt(DamageUtil.bloodDrainDamage(attacker), bloodDrainDamage);
        if (hurt) {
            int effectsLvl = attacker.level.getDifficulty().getId() - 1;
            if (effectsLvl >= 0) {
                for (Effect effect : BLOOD_DRAIN_EFFECTS) {
                    int duration = MathHelper.floor(20F * bloodDrainDamage);
                    EffectInstance effectInstance = target.getEffect(effect);
                    EffectInstance newInstance = effectInstance == null ? 
                            new EffectInstance(effect, duration, effectsLvl)
                            : new EffectInstance(effect, effectInstance.getDuration() + duration, effectsLvl);
                    target.addEffect(newInstance);
                }
            }
        }
        return hurt;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public double getMaxRangeSqEntityTarget() {
        return 4;
    }
    
    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.VAMPIRE_BLOOD_DRAIN.get(), 1.0F, 1.0F, true, user, power, this);
        }
    }
    
    @Override
    public boolean heldAllowsOtherAction(INonStandPower power, Action<INonStandPower> action) {
        return true;
    }
    
    @Override
    protected int maxCuringStage() {
        return 2;
    }
}
