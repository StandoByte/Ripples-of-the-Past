package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonCharacterTechniquePacket;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.MainHamonSkillsManager;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistry;

public class HamonTechniqueManager implements IHamonSkillsManager<CharacterTechniqueHamonSkill> {
    private final Set<CharacterTechniqueHamonSkill> wrappedSkillSet = new LinkedHashSet<>();
    private CharacterHamonTechnique technique = null;
    
    public HamonTechniqueManager() {}

    public Collection<CharacterTechniqueHamonSkill> getLearnedSkills() {
        return Collections.unmodifiableSet(wrappedSkillSet);
    }

    @Override
    public void addSkill(CharacterTechniqueHamonSkill skill) {
        if (!isCurrentTechniquePerk(skill) && !wrappedSkillSet.contains(skill)) {
            wrappedSkillSet.add(skill);
        }
    }

    @Override
    public void removeSkill(CharacterTechniqueHamonSkill skill) {
        wrappedSkillSet.remove(skill);
    }
    
    @Override
    public boolean containsSkill(CharacterTechniqueHamonSkill skill) {
        return isCurrentTechniquePerk(skill) || wrappedSkillSet.contains(skill);
    }

    private static final ActionConditionResult TECHNIQUE_LOCKED = ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.technique.locked"));
    private static final ActionConditionResult TECHNIQUE_MAX = ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.technique.max"));
    private static final ActionConditionResult WRONG_TECHNIQUE = ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.technique.bug"));
    @Override
    public ActionConditionResult canLearnSkill(LivingEntity user, HamonData hamon, CharacterTechniqueHamonSkill skill) {
        boolean clientSide = user.level.isClientSide();
        if (!techniquesEnabled(clientSide)) {
            return ActionConditionResult.NEGATIVE;
        }
        
        if (!JojoModConfig.getCommonConfigInstance(clientSide).mixHamonTechniques.get()
                && skill.getTechnique() != null && skill.getTechnique() != this.technique) {
            return WRONG_TECHNIQUE;
        }
        
        if (atMaxTechniqueSkills(clientSide)) {
            return TECHNIQUE_MAX;
        }
        
        if (!hamon.hasTechniqueLevel(wrappedSkillSet.size(), clientSide)) {
            return TECHNIQUE_LOCKED;
        }
        
        return ActionConditionResult.POSITIVE;
    }
    
    private boolean atMaxTechniqueSkills(boolean clientSide) {
        return wrappedSkillSet.size() >= techniqueSlotsCount(clientSide);
    }
    
    public CharacterHamonTechnique getTechnique() {
        return technique;
    }
    
    public boolean canPickTechnique(LivingEntity user) {
        return techniquesEnabled(user.level.isClientSide()) && this.technique == null;
    }
    
    public void setTechnique(CharacterHamonTechnique technique) {
        this.technique = technique;
    }
    
    public void resetTechnique() {
        technique = null;
    }
    
    public void addPerks(LivingEntity user, HamonData hamon) {
        if (getTechnique() != null && !user.level.isClientSide() || user.is(ClientUtil.getClientPlayer())) {
            getTechnique().getPerksOnPick().forEach(perk -> {
                if (perk != null) {
                    hamon.addHamonSkill(user, perk, false, false);
                }
            });
        }
    }
    
    private boolean isCurrentTechniquePerk(CharacterTechniqueHamonSkill skill) {
        return technique != null && technique.getPerksOnPick().anyMatch(perk -> perk == skill);
    }
    
    
    
    public static int techniqueSlotsCount(boolean clientSide) {
        int count = JojoModConfig.getCommonConfigInstance(clientSide).techniqueSkillsRequirement.get().size();
        return count;
    }
    
    public static boolean techniquesEnabled(boolean clientSide) {
        return techniqueSlotsCount(clientSide) > 0;
    }
    
    public static int techniqueSkillRequirement(int i, boolean clientSide) {
        return JojoModConfig.getCommonConfigInstance(clientSide).techniqueSkillsRequirement.get().get(i);
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        ListNBT skillsList = new ListNBT();
        wrappedSkillSet.forEach(skill -> {
            CompoundNBT skillNbt = new CompoundNBT();
            skillNbt.putString("Name", skill.getRegistryName().toString());
            skillsList.add(skillNbt);
        });
        nbt.put("Skills", skillsList);
        
        if (technique != null) {
            nbt.putString("CharacterTechnique", technique.getRegistryName().toString());
        }
        
        return nbt;
    }
    
    public void fromNBT(MainHamonSkillsManager mainSkillsHolder, CompoundNBT nbt) {
        ListNBT skillsNbt = nbt.getList("Skills", MCUtil.getNbtId(CompoundNBT.class));
        skillsNbt.forEach(entry -> {
            if (entry instanceof CompoundNBT) {
                CompoundNBT skillNbt = (CompoundNBT) entry;
                if (skillNbt.contains("Name", MCUtil.getNbtId(StringNBT.class))) {
                    AbstractHamonSkill s = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValue(new ResourceLocation(skillNbt.getString("Name")));
                    if (s instanceof CharacterTechniqueHamonSkill) {
                        CharacterTechniqueHamonSkill skill = (CharacterTechniqueHamonSkill) s;
                        mainSkillsHolder.addSkill(skill);
                    }
                }
            }
        });
        
        if (nbt.contains("CharacterTechnique", MCUtil.getNbtId(StringNBT.class))) {
            ResourceLocation techniqueId = new ResourceLocation(nbt.getString("CharacterTechnique"));
            IForgeRegistry<CharacterHamonTechnique> registry = JojoCustomRegistries.HAMON_CHARACTER_TECHNIQUES.getRegistry();
            if (registry.containsKey(techniqueId)) {
                this.technique = registry.getValue(techniqueId);
                
                if (technique != null) {
                    technique.getPerksOnPick().forEach(perk -> {
                        mainSkillsHolder.addSkill(perk);
                    });
                }
            }
        }
    }
    
    public void syncWithUser(ServerPlayerEntity user, HamonData hamon) {}
    
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity tracking, HamonData hamon) {
        if (getTechnique() != null) {
            PacketManager.sendToClient(new TrHamonCharacterTechniquePacket(user.getId(), getTechnique()), tracking);
        }
    }
    
    
    
    public static class Accessor {
        private final HamonTechniqueManager techniqueData;
        
        public Accessor(HamonTechniqueManager techniqueData) {
            this.techniqueData = techniqueData;
        }
        
        public Collection<CharacterTechniqueHamonSkill> getLearnedSkills() {
            return techniqueData.getLearnedSkills();
        }
        
        public boolean canLearnNewTechniqueSkill(HamonData hamon, LivingEntity user) {
            boolean clientSide = user.level.isClientSide();
            return !techniqueData.atMaxTechniqueSkills(clientSide)
                    && hamon.hasTechniqueLevel(getLearnedSkills().size(), clientSide);
        }
    }
}
