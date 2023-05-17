package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CharacterHamonTechnique extends ForgeRegistryEntry<CharacterHamonTechnique> {
    private final String name;
    private final List<Supplier<CharacterTechniqueHamonSkill>> skills;
    private final List<Supplier<CharacterTechniqueHamonSkill>> perksOnPick;
    @Nullable private final Supplier<SoundEvent> musicOnPick;

    private CharacterHamonTechnique(Builder builder) {
        this.name = builder.name;
        this.skills = builder.skills;
        this.perksOnPick = builder.perksOnPick;
        this.musicOnPick = builder.musicOnPick;
        assignTechniqueToSkills();
    }
    
    public String getName() {
        return name;
    }
    
    @Nullable
    public SoundEvent getMusicOnPick() {
        return musicOnPick != null ? musicOnPick.get() : null;
    }
    
    public Stream<CharacterTechniqueHamonSkill> getSkills() {
        return skills.stream().map(Supplier::get);
    }
    
    public Stream<CharacterTechniqueHamonSkill> getPerksOnPick() {
        return perksOnPick.stream().map(Supplier::get);
    }
    
    private void assignTechniqueToSkills() {
        getSkills().forEach(this::assignTechniqueTo);
        getPerksOnPick().forEach(this::assignTechniqueTo);
    }
    
    private void assignTechniqueTo(CharacterTechniqueHamonSkill skill) {
        if (skill == null) return;
        
        if (skill.getTechnique() != null) {
            JojoMod.getLogger().warn("{} Hamon skill is in {} character technique list, but {} technique was already assigned to it!", 
                    skill.getRegistryName(), skill.getTechnique().getRegistryName(), this.getRegistryName());
        }
        else {
            skill.setTechnique(this);
        }
    }
    
    
    
    public static class Builder {
        private final String name;
        private final List<Supplier<CharacterTechniqueHamonSkill>> skills;
        private final List<Supplier<CharacterTechniqueHamonSkill>> perksOnPick = new ArrayList<>();
        @Nullable private Supplier<SoundEvent> musicOnPick;

        public Builder(String name, List<Supplier<CharacterTechniqueHamonSkill>> skills) {
            this.name = name;
            this.skills = skills;
        }
        
        public Builder perkOnPick(Supplier<CharacterTechniqueHamonSkill> perkOnPick) {
            this.perksOnPick.add(perkOnPick);
            return this;
        }
        
        public Builder musicOnPick(@Nullable Supplier<SoundEvent> musicOnPick) {
            this.musicOnPick = musicOnPick;
            return this;
        }
        
        public CharacterHamonTechnique build() {
            return new CharacterHamonTechnique(this);
        }
    }
}
