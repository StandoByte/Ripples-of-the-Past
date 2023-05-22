package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.non_stand.HamonAction;

public class BaseHamonSkill extends AbstractHamonSkill {
    private final HamonStat hamonStat;
    private final boolean unlockedByDefault;

    public BaseHamonSkill(Builder builder) {
        super(builder.rewardType, builder.rewardAction, builder.requiredSkills);
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
    
    
    
    public static class Builder {
        private final RewardType rewardType;
        private final HamonStat hamonStat;
        private @Nullable Supplier<? extends HamonAction> rewardAction = null;
        private boolean unlockedByDefault = false;
        private final List<Supplier<? extends AbstractHamonSkill>> requiredSkills = new ArrayList<>();
        
        public Builder(HamonStat hamonStat, RewardType rewardType) {
            this.hamonStat = hamonStat;
            this.rewardType = rewardType;
        }
        
        public Builder unlocks(Supplier<HamonAction> rewardAction) {
            this.rewardAction = rewardAction;
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
