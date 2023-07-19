package com.github.standobyte.jojo.action.non_stand;

import java.util.Collection;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;

public class HamonMetalSilverOverdrive extends HamonOverdrive {

    public HamonMetalSilverOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        return ActionConditionResult.noMessage(itemUsesMSO(user));
    }
    
    public static boolean itemUsesMSO(LivingEntity user) {
        ItemStack heldItemStack = user.getMainHandItem();
        if (heldItemStack.isEmpty()) {
            return false;
        }
        
        // tiered items (swords, axes, tools, sledgehammer, modded weapons)
        if (heldItemStack.getItem() instanceof TieredItem) {
            return true;
        }
        
        // other items dealing extra damage (trident, knife, potentially unique modded weapons)
        Collection<AttributeModifier> damageModifiers = heldItemStack
                .getItem().getAttributeModifiers(EquipmentSlotType.MAINHAND, heldItemStack).get(Attributes.ATTACK_DAMAGE);
        if (damageModifiers != null) {
            return damageModifiers.stream().anyMatch(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION && modifier.getAmount() > 0);
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
        return DamageUtil.dealHamonDamage(targetEntity, dmgAmount, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_SILVER.get()));
    }
}
