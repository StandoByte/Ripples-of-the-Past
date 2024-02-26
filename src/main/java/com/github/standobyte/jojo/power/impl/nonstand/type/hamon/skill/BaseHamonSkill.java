package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.non_stand.HamonAction;

public class BaseHamonSkill extends AbstractHamonSkill {
    private final HamonStat hamonStat;
    private final boolean unlockedByDefault;

    public BaseHamonSkill(Builder builder) {
        super(builder);
        this.hamonStat = builder.hamonStat;
        this.unlockedByDefault = builder.unlockedByDefault;
    }
    
    @Override
    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }
    
    @Override
    public boolean requiresTeacher() {
        return true;
    }
    
    @Override
    public SkillType getSkillType() {
        return SkillType.BASE;
    }

    public HamonStat getStat() {
        return hamonStat;
    }
    
    
    public static enum HamonStat {
        STRENGTH,
        CONTROL
    }
    
    
    
    public static class Builder extends AbstractHamonSkill.AbstractBuilder {
        private final HamonStat hamonStat;
        private boolean unlockedByDefault = false;
        
        public Builder(HamonStat hamonStat, RewardType rewardType) {
            super(rewardType);
            this.hamonStat = hamonStat;
        }
        
        public Builder unlocks(Supplier<? extends HamonAction> rewardAction) {
            this.rewardActions.put(rewardAction, false);
            return this;
        }
        
        public Builder unlockedByDefault() {
            this.unlockedByDefault = true;
            return this;
        }
        
        public Builder requiredSkill(Supplier<? extends AbstractHamonSkill> parentSkill) {
            if (parentSkill != null && !requiredSkills.contains(parentSkill)) {
                requiredSkills.add(parentSkill);
            }
            return this;
        }
        
        public BaseHamonSkill build() {
            return new BaseHamonSkill(this);
        }
    }
}
