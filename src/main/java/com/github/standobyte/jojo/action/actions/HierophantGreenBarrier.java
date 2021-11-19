package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.power.IPower;
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
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        if (performer instanceof HierophantGreenEntity) {
            HierophantGreenEntity stand = (HierophantGreenEntity) performer;
            if (stand.getPlacedBarriersCount() >= getMaxBarriersPlaceable((IStandPower) power)) {
                return conditionMessage("barrier");
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            HierophantGreenEntity stand = (HierophantGreenEntity) getPerformer(user, power);
            stand.attachBarrier(target.getBlockPos());
        }
    }
    
    private int getMaxBarriersPlaceable(IStandPower power) {
        return 10 + MathHelper.floor((float) (power.getExp() - getExpRequirement()) / (float) (IStandPower.MAX_EXP - getExpRequirement()) * 90F);
    }
    
    @Override
    public TranslationTextComponent getTranslatedName(IPower<?> power, String key) {
        IStandPower standPower = (IStandPower) power;
        IStandManifestation stand = standPower.getStandManifestation();
        int barriers = stand instanceof HierophantGreenEntity ? ((HierophantGreenEntity) stand).getPlacedBarriersCount() : 0;
        return new TranslationTextComponent(key, barriers, getMaxBarriersPlaceable(standPower));
    }

}
