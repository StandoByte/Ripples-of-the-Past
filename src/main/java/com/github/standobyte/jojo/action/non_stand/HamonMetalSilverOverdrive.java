package com.github.standobyte.jojo.action.non_stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TieredItem;
import net.minecraft.util.SoundEvent;

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
        return !heldItemStack.isEmpty() && heldItemStack.getItem() instanceof TieredItem;
    }

    @Override
    @Nullable
    protected SoundEvent getShout(LivingEntity user, INonStandPower power, ActionTarget target, boolean wasActive) {
        ItemStack heldItemStack = user.getMainHandItem();
        if (heldItemStack.getItem() instanceof SwordItem
                && heldItemStack.hasCustomHoverName()
                && "pluck".equals(heldItemStack.getHoverName().getString().toLowerCase())) {
            return ModSounds.JONATHAN_PLUCK_SWORD.get();
        }
        return null;
    }
    
    @Override
    protected boolean dealDamage(LivingEntity target, float dmgAmount, LivingEntity user, INonStandPower power, HamonData hamon) {
        return DamageUtil.dealHamonDamage(target, dmgAmount, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_SILVER.get()));
    }
}
