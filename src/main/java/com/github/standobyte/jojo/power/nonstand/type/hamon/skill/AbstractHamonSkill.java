package com.github.standobyte.jojo.power.nonstand.type.hamon.skill;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class AbstractHamonSkill extends ForgeRegistryEntry<AbstractHamonSkill> {
    private final String name;
    private final RewardType rewardType;
    private final @Nullable Supplier<? extends HamonAction> rewardAction;
    private final List<Supplier<? extends AbstractHamonSkill>> requiredSkills;

    protected AbstractHamonSkill(String name, RewardType rewardType, @Nullable Supplier<? extends HamonAction> rewardAction, 
            List<Supplier<? extends AbstractHamonSkill>> requiredSkills) {
        this.name = name;
        this.rewardType = rewardType;
        this.rewardAction = rewardAction;
        this.requiredSkills = requiredSkills;
    }

    public String getName() {
        return name;
    }
    
    public RewardType getRewardType() {
        return rewardType;
    }
    
    public @Nullable HamonAction getRewardAction() {
        return rewardAction != null ? rewardAction.get() : null;
    }
    
    public boolean isUnlockedByDefault() {
        return false;
    }
    
    public boolean requiresTeacher() {
        return false;
    }
    
    public Stream<AbstractHamonSkill> getRequiredSkills() {
        return requiredSkills.stream().map(Supplier::get);
    }
    
    public abstract SkillType getSkillType();
    
    public boolean isBaseSkill() {
        return getSkillType() == SkillType.BASE;
    }
    
    public void learnNewSkill(HamonData hamon, LivingEntity user) {
        hamon.addHamonSkill(user, this, true, true);
    }
    
    
    
    
    
    public enum RewardType {
        ATTACK("attack", ActionType.ATTACK),
        ABILITY("ability", ActionType.ABILITY),
        PASSIVE("passive", null),
        ITEM("item", null);
        
        private final ITextComponent name;
        private final ActionType actionType;
        
        private RewardType(String key, ActionType actionType) {
            this.name = new TranslationTextComponent("hamon.skill_type." + key).withStyle(TextFormatting.ITALIC);
            this.actionType = actionType;
        }
        
        public ITextComponent getName() {
            return name;
        }
        
        @Nullable
        public ActionType getActionType() {
            return actionType;
        }
    }
    
    public enum SkillType {
        BASE,
        TECHNIQUE,
        CHARACTER_TECHNIQUE;
    }
}
