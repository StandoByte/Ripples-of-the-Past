package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonScarletOverdrive extends HamonAction {

    public HamonScarletOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        ItemStack heldItemStack = user.getMainHandItem();
        if (!heldItemStack.isEmpty()) {
            return conditionMessage("hand");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            if (target.getType() == TargetType.ENTITY) {
                HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
                Entity targetEntity = target.getEntity(user.level);
                if (ModDamageSources.dealHamonDamage(targetEntity, 0.1F, user, null)) {
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, getEnergyCost(power));
                    targetEntity.setSecondsOnFire(MathHelper.floor(2 + 8F * (float) hamon.getHamonStrengthLevel() / (float) HamonData.MAX_STAT_LEVEL));
                }
                user.doHurtTarget(targetEntity);
            }
        }
    }
}
