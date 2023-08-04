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
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public abstract class HamonAction extends NonStandAction {
    private final Map<Supplier<CharacterHamonTechnique>, Supplier<SoundEvent>> voiceLinesUnregistered;
    private Map<CharacterHamonTechnique, Supplier<SoundEvent>> voiceLines = null;
    
    public HamonAction(HamonAction.AbstractBuilder<?> builder) {
        super(builder);
        voiceLinesUnregistered = builder.voiceLines;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        ActionConditionResult hamonCheck = power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.getBloodstreamEfficiency() <= 0F) {
                return conditionMessage("hamon_no_bloodstream");
            }
            if (hamon.isMeditating()) {
                return ActionConditionResult.NEGATIVE;
            }
            return ActionConditionResult.POSITIVE;
        }).orElse(ActionConditionResult.NEGATIVE);
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
            shout = getCharacterShout(technique);
        }
        if (shout == null) {
            shout = super.getShout(user, power, target, wasActive);
        }
        return shout;
    }
    
    public SoundEvent getCharacterShout(CharacterHamonTechnique character) {
        if (voiceLines == null) {
            voiceLines = voiceLinesUnregistered.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().get(), entry -> entry.getValue()));
        }
        Supplier<SoundEvent> shoutSupplier = voiceLines.get(character);
        if (shoutSupplier != null) {
            return shoutSupplier.get();
        }
        return null;
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
    
    @Override
    public IFormattableTextComponent getNameLocked(INonStandPower power) {
        AbstractHamonSkill skill = getSkillToUnlock();
        return skill != null ? new TranslationTextComponent("jojo.layout_edit.locked.hamon_skill", skill.getNameTranslated())
                : super.getNameLocked(power);
    }
    
    private AbstractHamonSkill getSkillToUnlock() {
        return null;
    }
    
    @Override
    public void onPerform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        super.onPerform(world, user, power, target);
        if (swingHand() && user instanceof PlayerEntity) {
            ((PlayerEntity) user).resetAttackStrengthTicker();
        }
    }
    
    
    
    public static void addPointsForAction(INonStandPower power, HamonData hamon, HamonStat stat, float energyCost, float hamonEfficiency) {
        hamon.hamonPointsFromAction(stat, Math.min(energyCost, power.getEnergy()) * hamonEfficiency);
    }
    
    
    
    public static class Builder extends HamonAction.AbstractBuilder<HamonAction.Builder> {

        @Override
        protected HamonAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends NonStandAction.AbstractBuilder<T>> extends NonStandAction.AbstractBuilder<T> {
        protected final Map<Supplier<CharacterHamonTechnique>, Supplier<SoundEvent>> voiceLines = new HashMap<>();
        
        public T shout(Supplier<CharacterHamonTechnique> technique, Supplier<SoundEvent> shoutSupplier) {
            if (technique != null) {
                voiceLines.put(technique, shoutSupplier);
            }
            return getThis();
        }
    }
}
