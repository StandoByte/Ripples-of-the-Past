package com.github.standobyte.jojo.power.nonstand.type.hamon.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.non_stand.HamonAction;

public class CharacterTechniqueHamonSkill extends AbstractHamonSkill {
    private CharacterHamonTechnique technique;
    
    public CharacterTechniqueHamonSkill(Builder builder) {
        super(builder.name, builder.rewardType, builder.rewardAction, builder.requiredSkills);
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
    
    
    
    public static class Builder {
        private final String name;
        private final RewardType rewardType;
        private @Nullable Supplier<? extends HamonAction> rewardAction = null;
        private final List<Supplier<? extends AbstractHamonSkill>> requiredSkills = new ArrayList<>();
        
        public Builder(String name, RewardType rewardType) {
            this.name = name;
            this.rewardType = rewardType;
        }
        
        public Builder unlocks(Supplier<? extends HamonAction> rewardAction) {
            this.rewardAction = rewardAction;
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
