package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SnakeMufflerEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;

public class HamonSnakeMuffler {
    
    public static boolean snakeMuffler(LivingEntity target, DamageSource dmgSource, float dmgAmount) {
        if (!target.level.isClientSide() && target.canUpdate() && target.isOnGround()) {
            Entity attacker = dmgSource.getEntity();
            if (attacker != null && dmgSource.getDirectEntity() == attacker && attacker instanceof LivingEntity
                    && target instanceof PlayerEntity && target.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.SATIPOROJA_SCARF.get()) {
                LivingEntity livingAttacker = (LivingEntity) attacker;
                PlayerEntity playerTarget = (PlayerEntity) target;
                if (!playerTarget.getCooldowns().isOnCooldown(ModItems.SATIPOROJA_SCARF.get())) {
                    INonStandPower power = INonStandPower.getPlayerNonStandPower(playerTarget);
                    float energyCost = 500F;
                    if (power.hasEnergy(energyCost)) {
                        if (power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                            if (hamon.isSkillLearned(ModHamonSkills.SNAKE_MUFFLER.get())) {
                                playerTarget.getCooldowns().addCooldown(ModItems.SATIPOROJA_SCARF.get(), 80);
                                float efficiency = hamon.getActionEfficiency(energyCost, false, ModHamonSkills.SNAKE_MUFFLER.get());
                                if (efficiency == 1 || efficiency >= dmgAmount / target.getMaxHealth()) {
                                    JojoModUtil.sayVoiceLine(target, ModSounds.LISA_LISA_SNAKE_MUFFLER.get());
                                    power.consumeEnergy(energyCost);
                                    DamageUtil.dealHamonDamage(attacker, 0.75F, target, null);
                                    livingAttacker.addEffect(new EffectInstance(Effects.GLOWING, 200));
                                    SnakeMufflerEntity snakeMuffler = new SnakeMufflerEntity(target.level, target);
                                    snakeMuffler.setEntityToJumpOver(attacker);
                                    target.level.addFreshEntity(snakeMuffler);
                                    snakeMuffler.attachToBlockPos(target.blockPosition());
                                    return true;
                                }
                            }
                            return false;
                        }).orElse(false)) return true;
                    }
                }
            }
        }
        return false;
    }
}
