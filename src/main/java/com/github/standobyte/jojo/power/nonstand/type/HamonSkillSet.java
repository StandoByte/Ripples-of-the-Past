package com.github.standobyte.jojo.power.nonstand.type;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;

public class HamonSkillSet {
    final Set<HamonSkill> wrappedSkillSet;
    Set<Action> unlockedActions;
    int spentStrengthPoints = 0;
    int spentControlPoints = 0;
    HamonSkill.Technique technique = null;
    int techniqueSkillsLearned = 0;
    
    public HamonSkillSet() {
        wrappedSkillSet = EnumSet.noneOf(HamonSkill.class);
        unlockedActions = new HashSet<Action>();
    }
    
    public boolean isSkillLearned(HamonSkill skill) {
        return wrappedSkillSet.contains(skill);
    }
    
    public boolean parentsLearned(HamonSkill skill) {
       return skill.getParent1() == null || (wrappedSkillSet.contains(skill.getParent1()) && (skill.getParent2() == null || wrappedSkillSet.contains(skill.getParent2())));
    }
    
    public void addSkill(HamonSkill skill) {
        if (!wrappedSkillSet.contains(skill)) {
            HamonSkill.Technique technique = skill.getTechnique();
            if (technique == null) {
                if (skill.getStat() == HamonStat.STRENGTH && skill != HamonSkill.OVERDRIVE) {
                    spentStrengthPoints++;
                }
                else if (skill.getStat() == HamonStat.CONTROL && skill != HamonSkill.HEALING) {
                    spentControlPoints++;
                }
            }
            else {
                if (this.technique == null) {
                    this.technique = technique; 
                }
                if (this.technique == technique) {
                    techniqueSkillsLearned++;
                }
            }
            wrappedSkillSet.add(skill);
            Action action = skill.getRewardAction();
            if (action != null) {
                unlockedActions.add(action);
                if (action.getShiftVariationIfPresent() != action) {
                    unlockedActions.add(action.getShiftVariationIfPresent());
                }
            }
        }
    }
    
    public void removeSkill(HamonSkill skill) {
        if (skill != HamonSkill.OVERDRIVE && skill != HamonSkill.HEALING && wrappedSkillSet.contains(skill)) {
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
                if (this.technique == technique) {
                    techniqueSkillsLearned--;
                }
                if (techniqueSkillsLearned == 0) {
                    this.technique = null; 
                }
            }
            wrappedSkillSet.remove(skill);
            Action action = skill.getRewardAction();
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
    
    public static final int TECHNIQUE_MINIMAL_STAT_LVL = 20;
    public int getTechniqueLevelReq() {
        return techniqueLevelReq(techniqueSkillsLearned);
    }
    
    public static int techniqueLevelReq(int skillsLearned) {
        return skillsLearned * 10 + TECHNIQUE_MINIMAL_STAT_LVL;
    }
}
