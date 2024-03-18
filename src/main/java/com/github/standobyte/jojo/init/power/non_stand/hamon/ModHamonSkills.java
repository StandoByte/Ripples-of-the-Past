package com.github.standobyte.jojo.init.power.non_stand.hamon;

import java.util.ArrayList;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill.RewardType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterTechniqueHamonSkill;

import net.minecraft.util.Util;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class ModHamonSkills {
    public static final DeferredRegister<AbstractHamonSkill> HAMON_SKILLS = DeferredRegister.create(
            AbstractHamonSkill.class, JojoMod.MOD_ID);
    
    public static final DeferredRegister<CharacterHamonTechnique> HAMON_CHARACTER_TECHNIQUES = DeferredRegister.create(
            CharacterHamonTechnique.class, JojoMod.MOD_ID);
    
    
    
    public static final RegistryObject<BaseHamonSkill> OVERDRIVE = HAMON_SKILLS.register("overdrive", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.PASSIVE)
            .unlockedByDefault().build());
    
    public static final RegistryObject<BaseHamonSkill> SENDO_OVERDRIVE = HAMON_SKILLS.register("sendo_overdrive", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.ATTACK)
            .unlocks(ModHamonActions.HAMON_SENDO_OVERDRIVE)
            .requiredSkill(OVERDRIVE).build());
    
    public static final RegistryObject<BaseHamonSkill> TURQUOISE_BLUE_OVERDRIVE = HAMON_SKILLS.register("turquoise_blue_overdrive", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.ATTACK)
            .unlocks(ModHamonActions.HAMON_TURQUOISE_BLUE_OVERDRIVE)
            .requiredSkill(OVERDRIVE).build());
    
    public static final RegistryObject<BaseHamonSkill> SUNLIGHT_YELLOW_OVERDRIVE = HAMON_SKILLS.register("sunlight_yellow_overdrive", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.ATTACK)
            .unlocks(ModHamonActions.HAMON_SUNLIGHT_YELLOW_OVERDRIVE)
            .requiredSkill(SENDO_OVERDRIVE).requiredSkill(TURQUOISE_BLUE_OVERDRIVE).build());

    
    public static final RegistryObject<BaseHamonSkill> THROWABLES_INFUSION = HAMON_SKILLS.register("throwables_infusion", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.PASSIVE)
            .build());
    
    public static final RegistryObject<BaseHamonSkill> PLANT_BLOCK_INFUSION = HAMON_SKILLS.register("plant_infusion", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.ATTACK)
            .unlocks(ModHamonActions.HAMON_PLANT_INFUSION)
            .requiredSkill(THROWABLES_INFUSION).build());
    
    public static final RegistryObject<BaseHamonSkill> PLANT_ITEM_INFUSION = HAMON_SKILLS.register("plant_item_infusion", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.PASSIVE)
            .requiredSkill(THROWABLES_INFUSION).build());
    
    public static final RegistryObject<BaseHamonSkill> ANIMAL_INFUSION = HAMON_SKILLS.register("animal_infusion", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.PASSIVE)
            .unlocks(ModHamonActions.HAMON_ORGANISM_INFUSION)
            .requiredSkill(PLANT_BLOCK_INFUSION).build());
    
    public static final RegistryObject<BaseHamonSkill> ARROW_INFUSION = HAMON_SKILLS.register("arrow_infusion", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.PASSIVE)
            .requiredSkill(PLANT_ITEM_INFUSION).build());
    
            
    public static final RegistryObject<BaseHamonSkill> ZOOM_PUNCH = HAMON_SKILLS.register("zoom_punch", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.ATTACK)
            .unlocks(ModHamonActions.HAMON_ZOOM_PUNCH)
            .build());
            
    public static final RegistryObject<BaseHamonSkill> JUMP = HAMON_SKILLS.register("jump", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.ABILITY)
            .requiredSkill(ZOOM_PUNCH).build());
            
    public static final RegistryObject<BaseHamonSkill> SPEED_BOOST = HAMON_SKILLS.register("speed_boost", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_SPEED_BOOST)
            .requiredSkill(ZOOM_PUNCH).build());
    
    public static final RegistryObject<BaseHamonSkill> AFTERIMAGES = HAMON_SKILLS.register("afterimages", 
            () -> new BaseHamonSkill.Builder(HamonStat.STRENGTH, RewardType.PASSIVE)
            .requiredSkill(JUMP).requiredSkill(SPEED_BOOST).build());
    
            
    public static final RegistryObject<BaseHamonSkill> HEALING = HAMON_SKILLS.register("healing", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_HEALING)
            .unlockedByDefault().build());
    
    public static final RegistryObject<BaseHamonSkill> PLANTS_GROWTH = HAMON_SKILLS.register("plants_growth", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.PASSIVE)
            .requiredSkill(HEALING).build());
        
    public static final RegistryObject<BaseHamonSkill> EXPEL_VENOM = HAMON_SKILLS.register("expel_venom", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.PASSIVE)
            .requiredSkill(HEALING).build());
    
    public static final RegistryObject<BaseHamonSkill> HEALING_TOUCH = HAMON_SKILLS.register("healing_touch", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.PASSIVE)
            .requiredSkill(PLANTS_GROWTH).requiredSkill(EXPEL_VENOM).build());
    
    
    public static final RegistryObject<BaseHamonSkill> WALL_CLIMBING = HAMON_SKILLS.register("wall_climbing", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_WALL_CLIMBING)
            .build());
    
    public static final RegistryObject<BaseHamonSkill> LIQUID_WALKING = HAMON_SKILLS.register("liquid_walking", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.PASSIVE)
            .build());
    
    public static final RegistryObject<BaseHamonSkill> LIFE_MAGNETISM = HAMON_SKILLS.register("life_magnetism", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_LIFE_MAGNETISM)
            .requiredSkill(WALL_CLIMBING).build());
    
    public static final RegistryObject<BaseHamonSkill> PROJECTILE_SHIELD = HAMON_SKILLS.register("projectile_shield", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_PROJECTILE_SHIELD)
            .requiredSkill(LIQUID_WALKING).build());
    
    public static final RegistryObject<BaseHamonSkill> PROTECTION = HAMON_SKILLS.register("protection", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_PROTECTION)
            .requiredSkill(LIFE_MAGNETISM).requiredSkill(PROJECTILE_SHIELD).build());

    public static final RegistryObject<BaseHamonSkill> DETECTOR = HAMON_SKILLS.register("detector", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_DETECTOR)
            .build());
    
    public static final RegistryObject<BaseHamonSkill> HYPNOSIS = HAMON_SKILLS.register("hypnosis", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.ABILITY)
            .unlocks(ModHamonActions.HAMON_HYPNOSIS)
            .requiredSkill(DETECTOR).build());
    
    public static final RegistryObject<BaseHamonSkill> HAMON_SHOCK = HAMON_SKILLS.register("hamon_shock", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.PASSIVE)
            .unlocks(ModHamonActions.HAMON_SHOCK)
            .requiredSkill(DETECTOR).build());
    
    public static final RegistryObject<BaseHamonSkill> HAMON_SPREAD = HAMON_SKILLS.register("hamon_spread", 
            () -> new BaseHamonSkill.Builder(HamonStat.CONTROL, RewardType.PASSIVE)
            .requiredSkill(HYPNOSIS).requiredSkill(HAMON_SHOCK).build());
    
    
    
    
    
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> NATURAL_TALENT = HAMON_SKILLS.register("natural_talent",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.PASSIVE).build());

    public static final RegistryObject<CharacterTechniqueHamonSkill> SCARLET_OVERDRIVE = HAMON_SKILLS.register("scarlet_overdrive",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.JONATHAN_SCARLET_OVERDRIVE)
            .requiredSkill(SUNLIGHT_YELLOW_OVERDRIVE).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> METAL_SILVER_OVERDRIVE = HAMON_SKILLS.register("metal_silver_overdrive",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.PASSIVE)
            .unlocks(ModHamonActions.JONATHAN_METAL_SILVER_OVERDRIVE, false)
            .requiredSkill(SENDO_OVERDRIVE).requiredSkill(TURQUOISE_BLUE_OVERDRIVE).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> OVERDRIVE_BARRAGE = HAMON_SKILLS.register("overdrive_barrage",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE)
            .requiredSkill(SPEED_BOOST).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE = HAMON_SKILLS.register("syo_barrage",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE, false)
            .requiredSkill(OVERDRIVE_BARRAGE).requiredSkill(SUNLIGHT_YELLOW_OVERDRIVE).build());
    
    public static final RegistryObject<CharacterHamonTechnique> CHARACTER_JONATHAN = HAMON_CHARACTER_TECHNIQUES.register("jonathan", 
            () -> new CharacterHamonTechnique.Builder("jonathan", Util.make(new ArrayList<>(), list -> {
                list.add(ModHamonSkills.SCARLET_OVERDRIVE);
                list.add(ModHamonSkills.METAL_SILVER_OVERDRIVE);
                list.add(ModHamonSkills.OVERDRIVE_BARRAGE);
            }))
            .perkOnPick(ModHamonSkills.NATURAL_TALENT)
            .musicOnPick(ModSounds.HAMON_PICK_JONATHAN).build());
    
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> DEEP_PASS = HAMON_SKILLS.register("deep_pass",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.PASSIVE).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> HAMON_CUTTER = HAMON_SKILLS.register("hamon_cutter",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.ZEPPELI_HAMON_CUTTER)
            .requiredSkill(THROWABLES_INFUSION).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> SENDO_WAVE_KICK = HAMON_SKILLS.register("sendo_wave_kick",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.ZEPPELI_SENDO_WAVE_KICK)
            .requiredSkill(JUMP).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> TORNADO_OVERDRIVE = HAMON_SKILLS.register("tornado_overdrive",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.ZEPPELI_TORNADO_OVERDRIVE)
            .requiredSkill(JUMP).requiredSkill(SPEED_BOOST).build());
    
    public static final RegistryObject<CharacterHamonTechnique> CHARACTER_ZEPPELI = HAMON_CHARACTER_TECHNIQUES.register("zeppeli", 
            () -> new CharacterHamonTechnique.Builder("zeppeli", Util.make(new ArrayList<>(), list -> {
                list.add(ModHamonSkills.HAMON_CUTTER);
                list.add(ModHamonSkills.SENDO_WAVE_KICK);
                list.add(ModHamonSkills.TORNADO_OVERDRIVE);
            }))
            .perkOnPick(ModHamonSkills.DEEP_PASS)
            .musicOnPick(ModSounds.HAMON_PICK_ZEPPELI).build());
    
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> ROPE_TRAP = HAMON_SKILLS.register("rope_trap",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ITEM)
            .build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> CLACKER_VOLLEY = HAMON_SKILLS.register("clacker_volley",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ITEM)
            .requiredSkill(SENDO_OVERDRIVE).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> CHEAT_DEATH = HAMON_SKILLS.register("cheat_death",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.PASSIVE)
            .build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> REBUFF_OVERDRIVE = HAMON_SKILLS.register("rebuff_overdrive",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.JOSEPH_REBUFF_OVERDRIVE)
            .requiredSkill(SPEED_BOOST).requiredSkill(PROTECTION).build());
    
    public static final RegistryObject<CharacterHamonTechnique> CHARACTER_JOSEPH = HAMON_CHARACTER_TECHNIQUES.register("joseph", 
            () -> new CharacterHamonTechnique.Builder("joseph", Util.make(new ArrayList<>(), list -> {
                list.add(ModHamonSkills.ROPE_TRAP);
                list.add(ModHamonSkills.CLACKER_VOLLEY);
                list.add(ModHamonSkills.REBUFF_OVERDRIVE);
            }))
            .musicOnPick(ModSounds.HAMON_PICK_JOSEPH).build());
    
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> CRIMSON_BUBBLE = HAMON_SKILLS.register("crimson_bubble",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.PASSIVE).build());

    public static final RegistryObject<CharacterTechniqueHamonSkill> BUBBLE_LAUNCHER = HAMON_SKILLS.register("bubble_launcher",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.CAESAR_BUBBLE_LAUNCHER)
            .requiredSkill(THROWABLES_INFUSION).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> BUBBLE_CUTTER = HAMON_SKILLS.register("bubble_cutter",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.CAESAR_BUBBLE_CUTTER).unlocks(ModHamonActions.CAESAR_BUBBLE_CUTTER_GLIDING, false)
            .requiredSkill(BUBBLE_LAUNCHER).requiredSkill(ARROW_INFUSION).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> BUBBLE_BARRIER = HAMON_SKILLS.register("bubble_barrier",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ATTACK)
            .unlocks(ModHamonActions.CAESAR_BUBBLE_BARRIER)
            .requiredSkill(BUBBLE_LAUNCHER).requiredSkill(HAMON_SHOCK).build());
    
    public static final RegistryObject<CharacterHamonTechnique> CHARACTER_CAESAR = HAMON_CHARACTER_TECHNIQUES.register("caesar", 
            () -> new CharacterHamonTechnique.Builder("caesar", Util.make(new ArrayList<>(), list -> {
                list.add(ModHamonSkills.BUBBLE_LAUNCHER);
                list.add(ModHamonSkills.BUBBLE_CUTTER);
                list.add(ModHamonSkills.BUBBLE_BARRIER);
            }))
            .perkOnPick(ModHamonSkills.CRIMSON_BUBBLE)
            .musicOnPick(ModSounds.HAMON_PICK_CAESAR).build());
    
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> AJA_STONE_KEEPER = HAMON_SKILLS.register("aja_stone_keeper",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ITEM)
            .build());

    public static final RegistryObject<CharacterTechniqueHamonSkill> SATIPOROJA_SCARF = HAMON_SKILLS.register("satiporoja_scarf",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ITEM)
            .requiredSkill(ANIMAL_INFUSION).build());
    
    public static final RegistryObject<CharacterTechniqueHamonSkill> SNAKE_MUFFLER = HAMON_SKILLS.register("snake_muffler",
            () -> new CharacterTechniqueHamonSkill.Builder(RewardType.ITEM)
            .requiredSkill(SATIPOROJA_SCARF).requiredSkill(DETECTOR).build());
    
    public static final RegistryObject<CharacterHamonTechnique> CHARACTER_LISA_LISA = HAMON_CHARACTER_TECHNIQUES.register("lisa_lisa", 
            () -> new CharacterHamonTechnique.Builder("lisa_lisa", Util.make(new ArrayList<>(), list -> {
                list.add(ModHamonSkills.AJA_STONE_KEEPER);
                list.add(ModHamonSkills.SATIPOROJA_SCARF);
                list.add(ModHamonSkills.SNAKE_MUFFLER);
            }))
            .musicOnPick(ModSounds.HAMON_PICK_LISA_LISA).build());

}
