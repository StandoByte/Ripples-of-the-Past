package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillAddPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkillSet;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterTechniqueHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.HamonTechniqueManager;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.IHamonSkillsManager;
import com.google.common.collect.Iterables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

public class MainHamonSkillsManager implements IHamonSkillsManager<AbstractHamonSkill> {
    private final BaseHamonSkillSet baseSkills = new BaseHamonSkillSet();
    private final HamonTechniqueManager technique = new HamonTechniqueManager();
    private Set<Action<INonStandPower>> unlockedActions = new HashSet<>();
    
    
    @Override
    public void addSkill(AbstractHamonSkill skill) {
        handlerForSkill(skill).addSkill();
        skill.getRewardActions().forEach(unlockedActions::add);
    }
    
    @Override
    public void removeSkill(AbstractHamonSkill skill) {
        handlerForSkill(skill).removeSkill();
        skill.getRewardActions().forEach(unlockedActions::remove);
    }
    
    @Override
    public boolean containsSkill(AbstractHamonSkill skill) {
        return handlerForSkill(skill).containsSkill();
    }
    
    public boolean isUnlockedFromSkills(Action<INonStandPower> action) {
        return unlockedActions.contains(action);
    }
    
    public Iterable<AbstractHamonSkill> getLearnedSkills() {
        return Iterables.concat(baseSkills.getLearnedSkills(), technique.getLearnedSkills());
    }
    
    
    
    BaseHamonSkillSet getBaseSkills() {
        return baseSkills;
    }
    
    HamonTechniqueManager getTechniqueData() {
        return technique;
    }
    
    
    
    CompoundNBT toNBT() {
        CompoundNBT skillsNbt = new CompoundNBT();
        for (BaseHamonSkill skill : baseSkills.getLearnedSkills()) {
            skillsNbt.putBoolean(skill.getName(), baseSkills.containsSkill(skill));
        }
        skillsNbt.put("Technique", technique.toNBT());
        return skillsNbt;
    }

    void fromNbt(CompoundNBT nbt) {
        fillBaseSkills(nbt);
        technique.fromNBT(this, nbt.getCompound("Technique"));
    }
    
    // bruh
    private void fillBaseSkills(CompoundNBT nbt) {
        for (AbstractHamonSkill skill : JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues()) {
            if (skill instanceof BaseHamonSkill && nbt.contains(skill.getName()) && nbt.getBoolean(skill.getName())) {
                addSkill(skill);
            }
        }
    }
    
    
    
    void syncWithUser(ServerPlayerEntity user, HamonData hamon) {
        for (AbstractHamonSkill skill : getLearnedSkills()) {
            if (containsSkill(skill)) {
                PacketManager.sendToClient(new HamonSkillAddPacket(skill), user);
            }
        }
        technique.syncWithUser(user, hamon);
    }
    
    void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity tracking, HamonData hamon) {
        technique.syncWithTrackingOrUser(user, tracking, hamon);
    }

    private static final ActionConditionResult PARENTS_NOT_LEARNED = ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.parents"));
    @Override
    public ActionConditionResult canLearnSkill(LivingEntity user, HamonData hamon, AbstractHamonSkill skill) {
        ActionConditionResult check = handlerForSkill(skill).canLearnSkill(user, hamon);
        
        if (check.isPositive() && !skill.getRequiredSkills().allMatch(hamon::isSkillLearned)) {
            return PARENTS_NOT_LEARNED;
        }
        
        return check;
    }

    private static final ActionConditionResult NO_TEACHER = ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.teacher.required"));
    private static final ActionConditionResult NO_TEACHER_SKILL = ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.teacher.no_skill"));
    ActionConditionResult canLearnSkill(LivingEntity user, HamonData hamon, AbstractHamonSkill skill, @Nullable Collection<? extends AbstractHamonSkill> teachersSkills) {
        ActionConditionResult checksWithoutTeacher = canLearnSkill(user, hamon, skill);
        if (checksWithoutTeacher.isPositive() && skill.requiresTeacher()) {
            if (teachersSkills == null) {
                return NO_TEACHER;
            }
            else if (!teachersSkills.contains(skill)) {
                return NO_TEACHER_SKILL;
            }
        }
        return checksWithoutTeacher;
    }
    
    
    
    private class HamonSkillHandler<T extends AbstractHamonSkill> {
        private final IHamonSkillsManager<T> manager;
        private final T skill;
        
        private HamonSkillHandler(IHamonSkillsManager<T> manager, T skill) {
            this.manager = manager;
            this.skill = skill;
        }
        
        private void addSkill() {
            manager.addSkill(skill);
        }
        
        private void removeSkill() {
            manager.removeSkill(skill);
        }
        
        private boolean containsSkill() {
            return manager.containsSkill(skill);
        }
        
        private ActionConditionResult canLearnSkill(LivingEntity user, HamonData hamon) {
            return manager.canLearnSkill(user, hamon, skill);
        }
    }
    
    private HamonSkillHandler<? extends AbstractHamonSkill> handlerForSkill(AbstractHamonSkill skill) {
        switch (skill.getSkillType()) {
        case BASE:
            return new HamonSkillHandler<>(baseSkills, (BaseHamonSkill) skill);
        case TECHNIQUE:
            // TODO 
            return null;
        case CHARACTER_TECHNIQUE:
            return new HamonSkillHandler<>(technique, (CharacterTechniqueHamonSkill) skill);
        default:
            throw new IllegalArgumentException("No assigned type for skill " + skill.getRegistryName());
        }
    }
}
