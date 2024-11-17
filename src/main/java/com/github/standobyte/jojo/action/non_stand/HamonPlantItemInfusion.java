package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class HamonPlantItemInfusion {

    public static void chargeItemEntity(PlayerEntity throwerPlayer, ItemEntity itemEntity) {
        if (!throwerPlayer.level.isClientSide()) {
            INonStandPower.getNonStandPowerOptional(throwerPlayer).ifPresent(power -> {
                if (power.getEnergy() > 0) {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        if (hamon.isSkillLearned(ModHamonSkills.PLANT_ITEM_INFUSION.get()) && HamonUtil.isItemLivingMatter(itemEntity.getItem())) {
                            hamon.consumeHamonEnergyTo(hamonEfficiency -> {
                                int chargeTicks = 100 + MathHelper.floor((float) (1100 * hamon.getHamonStrengthLevel())
                                        / (float) HamonData.MAX_STAT_LEVEL * hamonEfficiency * hamonEfficiency);
                                
                                itemEntity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> 
                                cap.setHamonCharge(hamon.getHamonDamageMultiplier() * hamonEfficiency, chargeTicks, throwerPlayer, 200));
                                
                                return null;
                            }, 200, ModHamonSkills.PLANT_ITEM_INFUSION.get());
                        }
                    });
                }
            });
        }
    }
}
