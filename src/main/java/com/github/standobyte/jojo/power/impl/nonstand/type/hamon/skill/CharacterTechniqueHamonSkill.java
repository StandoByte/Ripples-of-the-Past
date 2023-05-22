package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.non_stand.HamonAction;

public class CharacterTechniqueHamonSkill extends AbstractHamonSkill {
    private CharacterHamonTechnique technique;
    private final boolean addsActionToHUD;
    
    public CharacterTechniqueHamonSkill(Builder builder) {
        super(builder.rewardType, builder.rewardAction, builder.requiredSkills);
        this.addsActionToHUD = builder.addsActionToHUD;
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
    
    @Override
    public boolean addsActionToHUD() {
        return addsActionToHUD;
    }
    
    
    
    public static class Builder {
        private final RewardType rewardType;
        private @Nullable Supplier<? extends HamonAction> rewardAction = null;
        private boolean addsActionToHUD = false;
        private final List<Supplier<? extends AbstractHamonSkill>> requiredSkills = new ArrayList<>();
        
        public Builder(RewardType rewardType) {
            this.rewardType = rewardType;
        }
        
        public Builder unlocks(Supplier<? extends HamonAction> rewardAction) {
            return unlocks(rewardAction, true);
        }
        
        public Builder unlocks(Supplier<? extends HamonAction> rewardAction, boolean addToHUD) {
            this.rewardAction = rewardAction;
            this.addsActionToHUD = addToHUD;
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
