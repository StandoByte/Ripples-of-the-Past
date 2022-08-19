package com.github.standobyte.jojo.action.non_stand;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction.AbstractBuilder;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

public abstract class HamonAction extends NonStandAction {
    private final Map<HamonSkill.Technique, Supplier<SoundEvent>> voiceLines;
    
    public HamonAction(HamonAction.Builder builder) {
        super(builder);
        voiceLines = builder.voiceLines;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).map(
                hamon -> hamon.getBloodstreamEfficiency() <= 0F)
                .orElseThrow(() -> new IllegalStateException("Non-Hamon users can't have Hamon actions!"))) {
            return conditionMessage("hamon_no_bloodstream");
        }
        return super.checkConditions(user, power, target);
    }

    @Override
    @Nullable
    protected SoundEvent getShout(LivingEntity user, INonStandPower power, ActionTarget target, boolean wasActive) {
        SoundEvent shout = super.getShout(user, power, target, wasActive);
        if (shout == null) {
            Technique technique = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get().getTechnique();
            if (technique != null) {
                Supplier<SoundEvent> shoutSupplier = voiceLines.get(technique);
                if (shoutSupplier != null) {
                    shout = shoutSupplier.get();
                }
            }
        }
        return shout;
    }
    
    
    
    public static class Builder extends NonStandAction.AbstractBuilder<HamonAction.Builder> {
        private Map<HamonSkill.Technique, Supplier<SoundEvent>> voiceLines = new EnumMap<>(HamonSkill.Technique.class);

        @Override
        protected HamonAction.Builder getThis() {
            return this;
        }
        
        public HamonAction.Builder shout(HamonSkill.Technique technique, Supplier<SoundEvent> shoutSupplier) {
            if (technique != null) {
                voiceLines.put(technique, shoutSupplier);
            }
            return getThis();
        }
    }
}
