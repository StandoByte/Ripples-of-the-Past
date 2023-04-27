package com.github.standobyte.jojo.action.non_stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TieredItem;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class HamonOverdrive extends HamonAction {

    public HamonOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        ItemStack heldItemStack = user.getMainHandItem();
        if (!heldItemStack.isEmpty() && !metalSilverOverdrive(power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get(), heldItemStack)) {
            return ActionConditionResult.NEGATIVE;
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public boolean sendsConditionMessage() {
        return false;
    }

    @Override
    @Nullable
    protected SoundEvent getShout(LivingEntity user, INonStandPower power, ActionTarget target, boolean wasActive) {
        HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
        ItemStack heldItemStack = user.getMainHandItem();
        if (metalSilverOverdrive(hamon, heldItemStack)) {
            if (heldItemStack.getItem() instanceof SwordItem && heldItemStack.hasCustomHoverName() && "pluck".equals(heldItemStack.getHoverName().getString().toLowerCase())) {
                return ModSounds.JONATHAN_PLUCK_SWORD.get();
            }
        }
        return null;
    }
    
    @Override
    public float getEnergyCost(INonStandPower power) {
        return Math.min(power.getEnergy(), getMaxEnergyCost(power));
    }
     
    private float getMaxEnergyCost(INonStandPower power) {
        float energyCost = super.getEnergyCost(power);
        if (metalSilverOverdrive(power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get(), power.getUser().getMainHandItem())) {
            energyCost += 250;
        }
        return energyCost;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            Entity entity = target.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity targetEntity = (LivingEntity) entity;
                HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
                
                float damage = 0.5F;
                float knockback = 0;
                
                if (metalSilverOverdrive(hamon, user.getMainHandItem())) {
//                    damage *= 0.75F;
                }
                else if (turquoiseBlueOverdrive(hamon, user, targetEntity)) {
                    damage *= 2F;
                    knockback = 1F;
                }
                float dmgScale = 1;
                if (user instanceof PlayerEntity) {
                    float swingStrengthScale = ((PlayerEntity) user).getAttackStrengthScale(0.5F);
                    dmgScale = (0.2F + swingStrengthScale * swingStrengthScale * 0.8F);
                }
                dmgScale *= getEnergyCost(power) / getMaxEnergyCost(power);
                damage *= dmgScale;
                if (DamageUtil.dealHamonDamage(targetEntity, damage, user, null)) {
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, getEnergyCost(power) * dmgScale);
                    if (knockback > 0) {
                    	DamageUtil.knockback(targetEntity, knockback, user.yRot);
                    }
                }
            }
        }
    }
    
    private boolean metalSilverOverdrive(HamonData hamon, ItemStack heldItemStack) {
        return hamon.isSkillLearned(HamonSkill.METAL_SILVER_OVERDRIVE) && heldItemStack.getItem() instanceof TieredItem;
    }
    
    private boolean turquoiseBlueOverdrive(HamonData hamon, LivingEntity user, LivingEntity target) {
        return hamon.isSkillLearned(HamonSkill.TURQUOISE_BLUE_OVERDRIVE) && user.isInWater() && target.isInWater();
    }

}
