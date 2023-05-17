package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class BaseHamonSkillSet implements IHamonSkillsManager<BaseHamonSkill> {
    private final Set<BaseHamonSkill> wrappedSkillSet = new HashSet<>();
    private int spentStrengthPoints = 0;
    private int spentControlPoints = 0;
    
    public BaseHamonSkillSet() {}

    public Collection<BaseHamonSkill> getLearnedSkills() {
        return Collections.unmodifiableSet(wrappedSkillSet);
    }

    @Override
    public void addSkill(BaseHamonSkill skill) {
        if (!wrappedSkillSet.contains(skill)) {
            if (!skill.isUnlockedByDefault()) {
                switch (skill.getStat()) {
                case STRENGTH:
                    spentStrengthPoints++;
                    break;
                case CONTROL:
                    spentControlPoints++;
                    break;
                }
            }
                
            wrappedSkillSet.add(skill);
        }
    }
    
    @Override
    public void removeSkill(BaseHamonSkill skill) {
        if (!skill.isUnlockedByDefault() && wrappedSkillSet.contains(skill)) {
            switch (skill.getStat()) {
            case STRENGTH:
                spentStrengthPoints--;
                break;
            case CONTROL:
                spentControlPoints--;
                break;
            }
            
            wrappedSkillSet.remove(skill);
        }
    }

    private static final ActionConditionResult NO_SKILL_POINTS = ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.points"));
    @Override
    public ActionConditionResult canLearnSkill(LivingEntity user, HamonData hamon, BaseHamonSkill skill) {
        if (hamon.getSkillPoints(skill.getStat()) <= 0) {
            return NO_SKILL_POINTS;
        }
        
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public boolean containsSkill(BaseHamonSkill skill) {
        return wrappedSkillSet.contains(skill);
    }
    
    public int getSpentStrengthPoints() {
        return spentStrengthPoints;
    }
    
    public int getSpentControlPoints() {
        return spentControlPoints;
    }
}
