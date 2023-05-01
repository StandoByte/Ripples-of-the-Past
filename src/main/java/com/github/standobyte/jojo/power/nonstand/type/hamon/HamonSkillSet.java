package com.github.standobyte.jojo.power.nonstand.type.hamon;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.HamonStat;

public class HamonSkillSet {
    final Set<HamonSkill> wrappedSkillSet;
    Set<Action<?>> unlockedActions;
    int spentStrengthPoints = 0;
    int spentControlPoints = 0;
    HamonSkill.Technique technique = null;
    int techniqueSkillsLearned = 0;
    
    public HamonSkillSet() {
        wrappedSkillSet = EnumSet.noneOf(HamonSkill.class);
        unlockedActions = new HashSet<>();
    }
    
    public boolean isSkillLearned(HamonSkill skill) {
        return wrappedSkillSet.contains(skill);
    }
    
    public boolean parentsLearned(HamonSkill skill) {
        return skill.getRequiredSkills().stream().allMatch(parent -> wrappedSkillSet.contains(parent));
    }
    
    public void addSkill(HamonSkill skill) {
        if (!wrappedSkillSet.contains(skill)) {
            HamonSkill.Technique technique = skill.getTechnique();
            if (technique == null && !skill.isUnlockedByDefault()) {
                if (skill.getStat() == HamonStat.STRENGTH) {
                    spentStrengthPoints++;
                }
                else if (skill.getStat() == HamonStat.CONTROL) {
                    spentControlPoints++;
                }
            }
            else {
                if (this.technique == null) {
                    this.technique = technique; 
                }
                if (technique != null) {
                    techniqueSkillsLearned++;
                }
            }
            wrappedSkillSet.add(skill);
            HamonAction action = skill.getRewardAction();
            if (action != null) {
                unlockedActions.add(action);
                if (action.getShiftVariationIfPresent() != action) {
                    unlockedActions.add(action.getShiftVariationIfPresent());
                }
            }
        }
    }
    
    public void removeSkill(HamonSkill skill) {
        if (!skill.isUnlockedByDefault() && wrappedSkillSet.contains(skill)) {
            HamonSkill.Technique technique = skill.getTechnique();
            if (technique == null) {
                if (skill.getStat() == HamonStat.STRENGTH) {
                    spentStrengthPoints--;
                }
                else if (skill.getStat() == HamonStat.CONTROL) {
                    spentControlPoints--;
                }
            }
            else {
                if (technique != null) {
                    techniqueSkillsLearned--;
                }
                if (techniqueSkillsLearned == 0) {
                    this.technique = null; 
                }
            }
            wrappedSkillSet.remove(skill);
            HamonAction action = skill.getRewardAction();
            if (action != null) {
                unlockedActions.remove(action);
                if (action.getShiftVariationIfPresent() != action) {
                    unlockedActions.remove(action.getShiftVariationIfPresent());
                }
            }
        }
    }
    
    public int getSpentStrengthPoints() {
        return spentStrengthPoints;
    }
    
    public int getSpentControlPoints() {
        return spentControlPoints;
    }

    public static final int MAX_TECHNIQUE_SKILLS = 3;
    public static final int TECHNIQUE_MINIMAL_STAT_LVL = 20;
    public int getTechniqueLevelReq() {
        return techniqueLevelReq(techniqueSkillsLearned);
    }
    
    public static int techniqueLevelReq(int skillsLearned) {
        return skillsLearned * 10 + TECHNIQUE_MINIMAL_STAT_LVL;
    }
}
