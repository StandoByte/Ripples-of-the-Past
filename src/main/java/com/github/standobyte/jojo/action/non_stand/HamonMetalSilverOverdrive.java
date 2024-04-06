package com.github.standobyte.jojo.action.non_stand;

import java.util.Arrays;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;

public class HamonMetalSilverOverdrive extends HamonOverdrive {

    public HamonMetalSilverOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
        if (target.getEntity() instanceof LivingEntity && !itemUsesMSO(target)) {
            return ModHamonActions.HAMON_OVERDRIVE.get();
        }
        return super.replaceAction(power, target);
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        return ActionConditionResult.noMessage(itemUsesMSO(target));
    }
  
    public static boolean itemUsesMSO(ActionTarget target) {
    	Entity entity = target.getEntity();
    	if(entity instanceof LivingEntity) {
        	LivingEntity targetEntity = (LivingEntity) entity;
        
        	ItemStack heldItemStack = targetEntity.getMainHandItem();
        	if (heldItemStack.isEmpty() && 
        			Arrays.stream(EquipmentSlotType.values()).allMatch(slot -> targetEntity.getItemBySlot(slot).isEmpty())) {
        	
        		return false;
        	}
        
        // tiered items (swords, axes, tools, sledgehammer, modded weapons)
        	if (heldItemStack.getItem() instanceof TieredItem || 
        			!Arrays.stream(EquipmentSlotType.values()).allMatch(slot -> targetEntity.getItemBySlot(slot).isEmpty())) {
        		return true;
        	}
        
        // other items dealing extra damage (trident, knife, potentially unique modded weapons)
        /*Collection<AttributeModifier> damageModifiers = heldItemStack
                .getItem().getAttributeModifiers(EquipmentSlotType.MAINHAND, heldItemStack).get(Attributes.ATTACK_DAMAGE);
        if (damageModifiers != null) {
            return damageModifiers.stream().anyMatch(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION && modifier.getAmount() > 0);
        }*/
        
        
    	}
    	return false;
    }
    
//    @Override
//    @Nullable
//    protected SoundEvent getShout(LivingEntity user, INonStandPower power, ActionTarget target, boolean wasActive) {
//        ItemStack heldItemStack = user.getMainHandItem();
//        if (heldItemStack.getItem() instanceof SwordItem
//                && heldItemStack.hasCustomHoverName()
//                && "pluck".equals(heldItemStack.getHoverName().getString().toLowerCase())) {
//            return ModSounds.JONATHAN_PLUCK_SWORD.get();
//        }
//        return null;
//    }
    
    @Override
    protected boolean dealDamage(ActionTarget target, LivingEntity targetEntity, float dmgAmount, LivingEntity user, INonStandPower power, HamonData hamon) {
    	float ratio = 1.0F;
    	ItemStack mainHandStack = targetEntity.getMainHandItem();
    	ItemStack offhandStack = targetEntity.getMainHandItem();
        for (int i = 0; i < 4; i++) {
            if (!targetEntity.getItemBySlot(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, i)).isEmpty()) {
                ratio += 0.5F;
            }
        }
    	
    	if (mainHandStack.getItem() instanceof TieredItem || offhandStack.getItem() instanceof TieredItem) {
    		ratio += 0.5F;
    	}
        return DamageUtil.dealHamonDamage(targetEntity, ratio * dmgAmount, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_SILVER.get()));
    }
}
