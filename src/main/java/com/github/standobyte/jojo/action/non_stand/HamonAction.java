package com.github.standobyte.jojo.action.non_stand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public abstract class HamonAction extends NonStandAction {
    private final Map<Supplier<CharacterHamonTechnique>, Supplier<SoundEvent>> voiceLinesUnregistered;
    private Map<CharacterHamonTechnique, Supplier<SoundEvent>> voiceLines = null;
    
    public HamonAction(HamonAction.Builder builder) {
        super(builder);
        voiceLinesUnregistered = builder.voiceLines;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        ActionConditionResult hamonCheck = power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.getBloodstreamEfficiency() <= 0F) {
                return conditionMessage("hamon_no_bloodstream");
            }
            return ActionConditionResult.POSITIVE;
        }).orElseThrow(() -> new IllegalStateException("Non-Hamon users can't have Hamon actions!"));
        if (!hamonCheck.isPositive()) {
            return hamonCheck;
        }
        
        return super.checkConditions(user, power, target);
    }
    
    @Override
    @Nullable
    protected SoundEvent getShout(LivingEntity user, INonStandPower power, ActionTarget target, boolean wasActive) {
        SoundEvent shout = null;
        CharacterHamonTechnique technique = power.getTypeSpecificData(ModPowers.HAMON.get()).get().getCharacterTechnique();
        if (technique != null) {
            if (voiceLines == null) {
                voiceLines = voiceLinesUnregistered.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().get(), entry -> entry.getValue()));
            }
            Supplier<SoundEvent> shoutSupplier = voiceLines.get(technique);
            if (shoutSupplier != null) {
                shout = shoutSupplier.get();
            }
        }
        if (shout == null) {
            shout = super.getShout(user, power, target, wasActive);
        }
        return shout;
    }
    
    @Override
    public void afterClick(World world, LivingEntity user, INonStandPower power, boolean passedRequirements) {
        if (changesAuraColor() && passedRequirements) {
            power.getTypeSpecificData(ModPowers.HAMON.get()).get().setLastUsedAction(this);
        }
    }
    
    protected boolean changesAuraColor() {
        return true;
    }
    
    
    
    public static class Builder extends NonStandAction.AbstractBuilder<HamonAction.Builder> {
        private Map<Supplier<CharacterHamonTechnique>, Supplier<SoundEvent>> voiceLines = new HashMap<>();
        
        @Override
        protected HamonAction.Builder getThis() {
            return this;
        }
        
        public HamonAction.Builder shout(Supplier<CharacterHamonTechnique> technique, Supplier<SoundEvent> shoutSupplier) {
            if (technique != null) {
                voiceLines.put(technique, shoutSupplier);
            }
            return getThis();
        }
    }
}
