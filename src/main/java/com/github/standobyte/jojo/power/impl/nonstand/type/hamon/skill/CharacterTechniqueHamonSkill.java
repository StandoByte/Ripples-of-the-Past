package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.non_stand.HamonAction;

public class CharacterTechniqueHamonSkill extends AbstractHamonSkill {
    private CharacterHamonTechnique technique;
    
    public CharacterTechniqueHamonSkill(Builder builder) {
        super(builder);
    }
    
    @Override
    public SkillType getSkillType() {
        return SkillType.CHARACTER_TECHNIQUE;
    }
    
    void setTechnique(CharacterHamonTechnique technique) {
        this.technique = technique;
    }
    
    public CharacterHamonTechnique getTechnique() {
        return technique;
    }
    
    
    
    public static class Builder extends AbstractHamonSkill.AbstractBuilder {
        
        public Builder(RewardType rewardType) {
            super(rewardType);
        }
        
        public Builder unlocks(Supplier<? extends HamonAction> rewardAction) {
            return unlocks(rewardAction, true);
        }
        
        public Builder unlocks(Supplier<? extends HamonAction> rewardAction, boolean addToHUD) {
            this.rewardActions.put(rewardAction, addToHUD);
            return this;
        }
        
        public Builder requiredSkill(Supplier<? extends AbstractHamonSkill> parentSkill) {
            if (parentSkill != null && !requiredSkills.contains(parentSkill)) {
                requiredSkills.add(parentSkill);
            }
            return this;
        }
        
        public CharacterTechniqueHamonSkill build() {
            return new CharacterTechniqueHamonSkill(this);
        }
    }
}
