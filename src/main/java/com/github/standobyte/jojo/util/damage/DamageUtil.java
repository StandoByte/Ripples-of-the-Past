package com.github.standobyte.jojo.util.damage;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.MathHelper;

public class DamageUtil {
    private static final DamageSource ULTRAVIOLET = new DamageSource("ultraviolet").bypassArmor();
    private static final String BLOOD_DRAIN_MSG = "bloodDrain";
    private static final DamageSource COLD = new DamageSource("cold").bypassArmor();
    public static final DamageSource HAMON = new DamageSource("hamon").bypassArmor();
    private static final DamageSource PILLAR_MAN_ABSORPTION = new DamageSource("pillarManAbsorption").setScalesWithDifficulty();
    public static final DamageSource STAND_VIRUS = new DamageSource("standVirus").bypassArmor();
    private static final String ROAD_ROLLER_MSG = "roadRoller";
    
    public static float knockbackReduction(DamageSource source) {
        if (source instanceof StandLinkDamageSource) {
            return 0;
        }
        if (source instanceof EntityDamageSource) {
            if (source.getDirectEntity() instanceof LivingEntity && 
                    INonStandPower.getNonStandPowerOptional((LivingEntity) source.getDirectEntity())
                    .map(power -> power.getHeldAction() == ModActions.JONATHAN_OVERDRIVE_BARRAGE.get()).orElse(false)) {
                return 0.1F;
            }
            String msgId = source.getMsgId();
            if (msgId != null && (msgId.startsWith(BLOOD_DRAIN_MSG) || msgId.startsWith(COLD.msgId) || msgId.startsWith(ROAD_ROLLER_MSG))) {
                return 0;
            }
            if (source instanceof IModdedDamageSource) {
                return ((IModdedDamageSource) source).getKnockbackFactor();
            }
        }
        return 1;
    }
    
    public static DamageSource bloodDrainDamage(Entity srcDirect) {
        return new EntityDamageSource(BLOOD_DRAIN_MSG, srcDirect);
    }

    public static boolean dealUltravioletDamage(Entity target, float amount, @Nullable Entity srcDirect, @Nullable Entity srcIndirect, boolean sun) {
        if (target instanceof LivingEntity && JojoModUtil.isUndead((LivingEntity) target) && !(sun && target.getType() == EntityType.WITHER)) {
            DamageSource dmgSource = srcDirect == null ? ULTRAVIOLET : 
                srcIndirect == null ? new EntityDamageSource(ULTRAVIOLET.getMsgId() + ".entity", srcDirect).bypassArmor().bypassMagic() : 
                new IndirectEntityDamageSource(ULTRAVIOLET.getMsgId() + ".entity", srcDirect, srcIndirect).bypassArmor().bypassMagic();
            return target.hurt(dmgSource, amount);
        }
        return false;
    }
    
    public static boolean isImmuneToCold(Entity target) {
        if (target.isInvulnerableTo(COLD)) {
            return true;
        }
        EntityType<?> type = target.getType();
        return type == EntityType.SNOW_GOLEM || type == EntityType.STRAY || type == EntityType.POLAR_BEAR;
    }
    
    public static boolean dealColdDamage(Entity target, float amount, @Nullable Entity srcDirect, @Nullable Entity srcIndirect) { // FIXME backport the vanilla mechanic
        if (target instanceof LivingEntity) {
            if (isImmuneToCold(target)) {
                return false;
            }
            EntityType<?> type = target.getType();
            if (type == EntityType.BLAZE || type == EntityType.MAGMA_CUBE || type == EntityType.STRIDER) {
                amount *= 5F;
            }
            else if (((LivingEntity) target).getMobType() == CreatureAttribute.UNDEAD) {
                amount *= 0.5F;
            }
            DamageSource dmgSource = srcDirect == null ? COLD : 
                srcIndirect == null ? new EntityDamageSource(COLD.getMsgId() + ".entity", srcDirect).bypassArmor() : 
                new IndirectEntityDamageSource(COLD.getMsgId() + ".entity", srcDirect, srcIndirect).bypassArmor();
            return target.hurt(dmgSource, amount);
        }
        return false;
    }

    public static boolean dealHamonDamage(Entity target, float amount, @Nullable Entity srcDirect, @Nullable Entity srcIndirect) {
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            boolean scarf = livingTarget.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.SATIPOROJA_SCARF.get();
            if (scarf) {
                if (INonStandPower.getNonStandPowerOptional(livingTarget)
                        .map(power -> power.getType() == ModNonStandPowers.HAMON.get()).orElse(false)) {
                    return false;
                }
                amount *= 0.25F;
            }
            DamageSource dmgSource = srcDirect == null ? HAMON : 
                    srcIndirect == null ? new EntityDamageSource(HAMON.getMsgId() + ".entity", srcDirect).bypassArmor() : 
                    new IndirectEntityDamageSource(HAMON.getMsgId() + ".entity", srcDirect, srcIndirect).bypassArmor();
            boolean undeadTarget = JojoModUtil.isUndead(livingTarget);
            if (!undeadTarget) {
                amount *= 0.1F;
            }
            final float dmgAmount = amount;
            if (dmgSource.getEntity() instanceof LivingEntity) {
                LivingEntity sourceLiving = (LivingEntity) dmgSource.getEntity();
                float hamonMultiplier = INonStandPower.getNonStandPowerOptional(sourceLiving).map(power -> 
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).map(hamon -> {
                    if (undeadTarget && !scarf && hamon.isSkillLearned(HamonSkill.HAMON_SPREAD)) {
                        float effectStr = (hamon.getHamonDamageMultiplier() - 1) / (HamonData.MAX_HAMON_DAMAGE - 1);
                        int effectDuration = 25 + MathHelper.floor(125F * effectStr);
                        int effectLvl = MathHelper.clamp(MathHelper.floor(1.5F * effectStr * dmgAmount), 0, 3);
                        livingTarget.addEffect(new EffectInstance(ModEffects.HAMON_SPREAD.get(), effectDuration, effectLvl));
                    }
                    return hamon.getHamonDamageMultiplier() * hamon.hamonEfficiency();
                }).orElse(1F)).orElse(1F);
                amount *= hamonMultiplier;
            }
            amount *= JojoModConfig.getCommonConfigInstance().hamonDamageMultiplier.get().floatValue();
            if (hurtThroughInvulTicks(target, dmgSource, amount)) {
                HamonPowerType.createHamonSparkParticlesEmitter(target, amount / HamonData.MAX_HAMON_DAMAGE);
                if (scarf && undeadTarget && livingTarget instanceof ServerPlayerEntity) {
                    ModCriteriaTriggers.VAMPIRE_HAMON_DAMAGE_SCARF.get().trigger((ServerPlayerEntity) livingTarget);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean dealPillarmanAbsorptionDamage(Entity target, float amount, @Nullable Entity src) {
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            if (!JojoModUtil.canBleed(livingTarget)) {
                return false;
            }
            boolean dealDamage = INonStandPower.getNonStandPowerOptional(livingTarget).map(power -> {
                if (!power.hasPower()) {
                    return true;
                }
                NonStandPowerType<?> powerType = power.getType();
                if (powerType == ModNonStandPowers.HAMON.get() && power.consumeEnergy(2F)) {
                    HamonPowerType.createHamonSparkParticles(target.level, null, target.getX(), target.getY(0.5), target.getZ(), 0.1F);
                    return false;
                }
                return true;
            }).orElse(true);
            if (!dealDamage) {
                return false;
            }
            DamageSource dmgSource = 
                    src == null ? PILLAR_MAN_ABSORPTION : new EntityDamageSource(PILLAR_MAN_ABSORPTION.getMsgId() + ".entity", src);
            return target.hurt(dmgSource, amount);
        }
        return false;
    }
    
    public static DamageSource roadRollerDamage(RoadRollerEntity entity) {
        return new EntityDamageSource(ROAD_ROLLER_MSG, entity);
    }
    
    public static boolean dealDamageAndSetOnFire(Entity entity, Predicate<Entity> hurtEntity, int fireSeconds, boolean stand) {
        int fireTicks = entity.getRemainingFireTicks();
        setOnFire(entity, fireSeconds, stand);
        boolean dealtDamage = hurtEntity.test(entity);
        if (!dealtDamage) {
            entity.setRemainingFireTicks(fireTicks);
        }
        return dealtDamage;
    }
    
    public static void setOnFire(Entity entity, int fireSeconds, boolean stand) {
        if (stand && entity instanceof StandEntity) {
            ((StandEntity) entity).setFireFromStand(fireSeconds);
        }
        else {
            entity.setSecondsOnFire(fireSeconds);
        }
    }
    
    public static boolean hurtThroughInvulTicks(Entity target, DamageSource dmgSource, float amount) {
        int invulTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        boolean dealtDamage = target.hurt(dmgSource, amount);
        target.invulnerableTime = invulTime;
        return dealtDamage;
    }
    
    public static float addArmorPiercing(float damage, float armorPiercing, @Nullable LivingEntity armoredTarget) {
        if (armoredTarget != null && armorPiercing > 0) {
            float armor = (float) armoredTarget.getArmorValue();
            if (armor > 0) {
                float toughness = (float) armoredTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                armorPiercing = MathHelper.clamp(armorPiercing, 0, 1);
                float damagePierced = MathHelper.lerp(armorPiercing, CombatRules.getDamageAfterAbsorb(damage, armor, toughness), damage);
                damage = MathUtil.inverseArmorProtectionDamage(damagePierced, armor, toughness);
            }
        }
        return damage;
    }
    
    public static void disableShield(PlayerEntity target, float chance) {
        if (!target.level.isClientSide() && target.getRandom().nextFloat() < chance) {
            target.getCooldowns().addCooldown(target.getUseItem().getItem(), 100);
            target.stopUsingItem();
            target.level.broadcastEntityEvent(target, (byte) 30);
        }
    }
}
