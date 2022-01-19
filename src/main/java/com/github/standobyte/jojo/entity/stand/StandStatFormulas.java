package com.github.standobyte.jojo.entity.stand;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.CombatRules;
import net.minecraft.util.math.MathHelper;

public class StandStatFormulas {

    public static final float getHeavyAttackDamage(double strength, @Nullable LivingEntity armoredTarget) {
        float damage = Math.max((float) strength, 1F);
        if (armoredTarget != null) {
            float armor = (float) armoredTarget.getArmorValue();
            float toughness = (float) armoredTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float armorPiercing = MathHelper.clamp(getHeavyAttackArmorPiercing(strength), 0, 1);
            float damagePierced = MathHelper.lerp(armorPiercing, CombatRules.getDamageAfterAbsorb(damage, armor, toughness), damage);
            // i'm too dumb to figure out the formula there
            while (CombatRules.getDamageAfterAbsorb(damage, armor, toughness) < damagePierced) {
                damage++;
            }
        }
        return damage;
    }
    
    
    
    private static final float getHeavyAttackArmorPiercing(double strength) {
        return (float) strength * 0.03125F;
    }
    
    public static final int getHeavyAttackWindup(double speed, float combo) {
        // FIXME (stats) combo
        return MathHelper.ceil((40 - speed * 1.25) * 2 / 3);
    }
    
    public static final int getHeavyAttackRecovery(double speed) {
        return MathHelper.floor((40 - speed * 1.25) / 3);
    }
    
    
    
    public static final float getLightAttackDamage(double strength) {
        return 0.5F + (float) strength * 0.25F;
    }
    
    public static final int getLightAttackWindup(double speed) {
        return Math.max(5 - MathHelper.log2((int) speed), 1);
    }
    
    public static final int getLightAttackComboDelay(double speed) {
        return MathHelper.floor((40.0 - speed * 1.25) / 6.0);
    }
    
    public static final int getLightAttackRecovery(double speed) {
        return MathHelper.floor((40.0 - speed * 1.25) / 6.0);
    }
    
    
    
    public static final float getBarrageHitDamage(double strength, double precision, Random random) {
        float damage = 0.04F + (float) strength * 0.01F;
        if (precision > 0 && random.nextDouble() < Math.min(precision, 0.5)) {
            damage *= 1 + (float) precision * 0.5F;
        }
        return damage;
    }
    
    public static final int getBarrageHitsPerSecond(double speed) {
        return Math.max((int) (speed * 7.5 - 20.0), 0);
    }
    
    public static final int getBarrageRecovery(double speed) {
        return MathHelper.floor((40.0 - speed * 1.25) / 6.0);
    }
    
    public static final int getBarrageMaxDuration(double durability) {
        return 20 + (int) (durability * 5.0);
    }
    
    
    
    public static final float getPhysicalResistance(double durability, double strength, boolean blocked) {
        double resistance = MathHelper.clamp(durability * 0.03 + strength * 0.02, 0, 1);
        if (blocked) {
            resistance += (1 - resistance) / 2;
        }
        return (float) resistance;
    }
    
    public static final float getStaminaMultiplier(double durability) {
        return (float) (500 * Math.pow(2, durability / 8));
    }
    
    public static final int getSummonLockTicks(double speed) {
        return Math.max(20 - (int) (speed * 1.25), 0);
    }
    
    public static final float rangeStrengthFactor(double rangeEffective, double rangeMax, double distance) {
        if (distance <= rangeEffective) {
            return 1F;
        }
        float f = (float) ((rangeMax - rangeEffective) / (2 * rangeEffective - rangeMax - distance));
        return f * f;
    }
    
    public static final float getLeapStrength(double strength) {
        return (float) Math.min(strength, 40) / 5F;
    }
    
    public static final double getMovementSpeed(double speed) {
        return 0.1 + speed * 0.05;
    }
    
    public static final boolean isBlockBreakable(double strength, float blockHardness, int blockHarvestLevel) { // FIXME block breaking progress (B-statted stand can break obsidian, but it's slow)
        /* damage:
         * 2                                4                                   8                                   12                                      16
         * 
         * hardness:
         * e^0.5 = 1.649                    e^1 ~ 2.718                         e^2 ~ 7.389                         e^3 ~ 20.086                            e^4 ~ 54.598
         * 
         * 0.3:  glass, glowstone           1.8:  concrete                      2.8:  blue ice                      10:   hardened glass                    22.5: ender chest
         * 0.4:  netherrack                 2:    bricks, cobblestone, wood     3.5:  furnace                                                               30:   ancient debris
         * 0.5:  dirt, ice, sand, hay       2.5:  chest                         3:    ores, gold/lapis block                                                50:   obsidian, netherite
         * 0.6:  clay, gravel                                                   4.5:  deepslate ores
         * 0.8:  quartz, sandstone, wool                                        5:    diamond/iron/redstone block
         * 1:    melon, mob head
         * 1.25: terracota, basalt 
         * 1.5:  stone 
         * 
         * harvest level:
         * -1: leaves/grass                 0: dirt/wood/stone                  1: iron                             2: diamond/gold                         3: obsidian/netherite
         */
        return blockHardness < Math.exp(strength / 4) && blockHarvestLevel < (int) strength / 4;
    }
    
    // parryTiming
    // dash
    // projectileAccuracy
    // hitboxExpansion
    // unsummonedAttackDeflectSpeed
    
}
