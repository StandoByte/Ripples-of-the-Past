package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HamonMetalSilverOverdrive extends HamonOverdrive {

    public HamonMetalSilverOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        return ActionConditionResult.noMessage(targetedByMSO(target));
    }
  
    public static boolean targetedByMSO(ActionTarget target) {
    	Entity entity = target.getEntity();
    	if (entity instanceof LivingEntity) {
    	    LivingEntity targetEntity = (LivingEntity) entity;
    	    return getDamageMultiplier(targetEntity) > 1;
    	}
    	return false;
    }
    
    @Override
    protected boolean dealDamage(ActionTarget target, LivingEntity targetEntity, float dmgAmount, LivingEntity user, INonStandPower power, HamonData hamon) {
    	float ratio = getDamageMultiplier(targetEntity);
        return DamageUtil.dealHamonDamage(targetEntity, ratio * dmgAmount, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_SILVER.get()));
    }
    
    private static float getDamageMultiplier(LivingEntity targetEntity) {
        float mult = 1;
        
        for (int i = 0; i < 4; i++) {
            if (!targetEntity.getItemBySlot(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, i)).isEmpty()) {
                mult += 0.2F;
            }
        }
        for (Hand hand : Hand.values()) {
            ItemStack heldStack = targetEntity.getItemInHand(hand);
            if (MCUtil.isItemWeapon(heldStack)) {
                mult += 0.2F;
                break;
            }
        }
        
        return mult;
    }
}
