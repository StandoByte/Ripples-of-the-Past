package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class HierophantGreenBarrier extends StandEntityAction {

    public HierophantGreenBarrier(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        if (performer instanceof HierophantGreenEntity) {
            HierophantGreenEntity stand = (HierophantGreenEntity) performer;
            if (stand.getPlacedBarriersCount() >= getMaxBarriersPlaceable(power)) {
                return conditionMessage("barrier");
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HierophantGreenEntity stand = (HierophantGreenEntity) getPerformer(user, power);
            stand.attachBarrier(target.getBlockPos());
        }
    }
    
    private int getMaxBarriersPlaceable(IStandPower power) {
        return 10 + MathHelper.floor((float) (power.getXp() - getXpRequirement()) / (float) (IStandPower.MAX_EXP - getXpRequirement()) * 90F);
    }
    
    @Override
    public TranslationTextComponent getTranslatedName(IStandPower power, String key) {
        IStandManifestation stand = power.getStandManifestation();
        int barriers = stand instanceof HierophantGreenEntity ? ((HierophantGreenEntity) stand).getPlacedBarriersCount() : 0;
        return new TranslationTextComponent(key, barriers, getMaxBarriersPlaceable(power));
    }

}
