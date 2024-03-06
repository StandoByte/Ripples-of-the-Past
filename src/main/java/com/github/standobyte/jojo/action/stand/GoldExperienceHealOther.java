package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class GoldExperienceHealOther extends GoldExperienceHeal {

    public GoldExperienceHealOther(Builder builder) {
        super(builder);
    }
    
    @Nullable
    @Override
    protected Action<IStandPower> replaceAction(IStandPower power, ActionTarget target) {
        if (target.getType() != TargetType.ENTITY || target.getEntity() == power.getUser()) {
            return ModStandsInit.GOLD_EXPERIENCE_HEALING_ITEM.get();
        }
        return this;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (target.getEntity() instanceof LivingEntity) {
            if (target.getEntity() == user) {
                return ActionConditionResult.NEGATIVE;
            }
            return canHeal((LivingEntity) target.getEntity(), user);
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;    
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide() && task.getTarget().getEntity() instanceof LivingEntity) {
            LivingEntity user = userPower.getUser();
            spendAndHeal(world, (LivingEntity) task.getTarget().getEntity(), user, userPower, standEntity);
        }
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        ActionTarget target = ActionsOverlayGui.getInstance().getMouseTarget();
        if (target.getEntity() != null) {
            return new TranslationTextComponent(key, target.getEntity().getName()); 
        }
        return super.getTranslatedName(power, key);
    }
    
    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        if (target.getEntity() instanceof LivingEntity) {
            String postfix = getPostfix((LivingEntity) target.getEntity());
            return postfix != null ? key + postfix : key;
        }
        return key;
    }
    
}
