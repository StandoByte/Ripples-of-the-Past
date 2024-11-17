package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.non_stand.HamonAction;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class BaseHamonSkill extends AbstractHamonSkill {
    private final HamonStat hamonStat;
    private final boolean unlockedByDefault;

    public BaseHamonSkill(Builder builder) {
        super(builder);
        this.hamonStat = builder.hamonStat;
        this.unlockedByDefault = builder.unlockedByDefault;
        builder.skillBranch.add(this);
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
    
    
    public static class SkillBranch {
        public static final SkillBranch OVERDRIVE = new SkillBranch("overdrive");
        public static final SkillBranch INFUSION = new SkillBranch("infusion");
        public static final SkillBranch FLEXIBILITY = new SkillBranch("flexibility");
        public static final SkillBranch HEALING = new SkillBranch("life");
        public static final SkillBranch ATTRACTANT_REPELLENT = new SkillBranch("attractant_repellent");
        public static final SkillBranch BODY_MANIPULATION = new SkillBranch("body_manipulation");
        
        public final ITextComponent name;
        public SkillBranch(String name) {
            this.name = new TranslationTextComponent("hamon.skills." + name);
        }
        
        private final List<AbstractHamonSkill> skills = new ArrayList<>();
        
        public void add(AbstractHamonSkill skill) {
            this.skills.add(skill);
        }
        
        public Collection<AbstractHamonSkill> getSkillsView() {
            return skills;
        }
    }
    
    
    
    public static class Builder extends AbstractHamonSkill.AbstractBuilder {
        private final HamonStat hamonStat;
        private boolean unlockedByDefault = false;
        protected SkillBranch skillBranch;
        
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
        
        public Builder skillBranch(SkillBranch skillBranch) {
            this.skillBranch = skillBranch;
            return this;
        }
        
        public BaseHamonSkill build() {
            return new BaseHamonSkill(this);
        }
    }
}
