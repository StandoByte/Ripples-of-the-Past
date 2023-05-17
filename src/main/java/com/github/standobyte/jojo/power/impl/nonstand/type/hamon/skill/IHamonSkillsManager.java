package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;

public interface IHamonSkillsManager<T extends AbstractHamonSkill> {
    void addSkill(T skill);
    void removeSkill(T skill);
    boolean containsSkill(T skill);
    ActionConditionResult canLearnSkill(LivingEntity user, HamonData hamon, T skill);
}
