package com.github.standobyte.jojo.power.nonstand.type.hamon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public enum HamonSkill {
    OVERDRIVE("overdrive", 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    SENDO_OVERDRIVE("sendo_overdrive", 
            HamonStat.STRENGTH, null, 
            RewardType.ATTACK, ModHamonActions.HAMON_SENDO_OVERDRIVE,
            OVERDRIVE),
    TURQUOISE_BLUE_OVERDRIVE("turquoise_blue_overdrive", 
            HamonStat.STRENGTH, null, 
            RewardType.ATTACK, ModHamonActions.HAMON_TURQUOISE_BLUE_OVERDRIVE,
            OVERDRIVE),
    SUNLIGHT_YELLOW_OVERDRIVE("sunlight_yellow_overdrive", 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null,
            SENDO_OVERDRIVE, TURQUOISE_BLUE_OVERDRIVE),
    
    THROWABLES_INFUSION("throwables_infusion", 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    PLANT_INFUSION("plant_infusion", 
            HamonStat.STRENGTH, null, 
            RewardType.ATTACK, ModHamonActions.HAMON_PLANT_INFUSION,
            THROWABLES_INFUSION),
    ARROW_INFUSION("arrow_infusion", 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null,
            THROWABLES_INFUSION),
    ANIMAL_INFUSION("animal_infusion", 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, ModHamonActions.HAMON_ORGANISM_INFUSION,
            PLANT_INFUSION, ARROW_INFUSION),
    
    ZOOM_PUNCH("zoom_punch", 
            HamonStat.STRENGTH, null, 
            RewardType.ATTACK, ModHamonActions.HAMON_ZOOM_PUNCH),
    JUMP("jump", 
            HamonStat.STRENGTH, null, 
            RewardType.ABILITY, null,
            ZOOM_PUNCH),
    SPEED_BOOST("speed_boost", 
            HamonStat.STRENGTH, null, 
            RewardType.ABILITY, ModHamonActions.HAMON_SPEED_BOOST,
            ZOOM_PUNCH),
    AFTERIMAGES("afterimages",
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null,
            JUMP, SPEED_BOOST),
    
    HEALING("healing", 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModHamonActions.HAMON_HEALING),
    PLANTS_GROWTH("plants_growth", 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null,
            HEALING),
    EXPEL_VENOM("expel_venom",
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null,
            HEALING),
    HEALING_TOUCH("healing_touch", 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null,
            PLANTS_GROWTH, EXPEL_VENOM),

    WALL_CLIMBING("wall_climbing", 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModHamonActions.HAMON_WALL_CLIMBING),
    DETECTOR("detector", 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModHamonActions.HAMON_DETECTOR,
            WALL_CLIMBING),
    LIFE_MAGNETISM("life_magnetism", 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModHamonActions.HAMON_LIFE_MAGNETISM,
            WALL_CLIMBING),
    HAMON_SPREAD("hamon_spread", 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null,
            DETECTOR, LIFE_MAGNETISM),

    WATER_WALKING("water_walking", 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null),
    PROJECTILE_SHIELD("projectile_shield", 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModHamonActions.HAMON_PROJECTILE_SHIELD,
            WATER_WALKING),
    LAVA_WALKING("lava_walking", 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null,
            WATER_WALKING),
    REPELLING_OVERDRIVE("repelling_overdrive", 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModHamonActions.HAMON_REPELLING_OVERDRIVE,
            PROJECTILE_SHIELD, LAVA_WALKING),
    

    NATURAL_TALENT("natural_talent", 
            null, Technique.JONATHAN, 
            RewardType.ATTACK, ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE,
            ZOOM_PUNCH),
    SCARLET_OVERDRIVE("scarlet_overdrive", 
            null, Technique.JONATHAN, 
            RewardType.ATTACK, ModHamonActions.JONATHAN_SCARLET_OVERDRIVE,
            SUNLIGHT_YELLOW_OVERDRIVE),
    METAL_SILVER_OVERDRIVE("metal_silver_overdrive", 
            null, Technique.JONATHAN, 
            RewardType.PASSIVE, null,
            SENDO_OVERDRIVE),
    
    HAMON_CUTTER("hamon_cutter", 
            null, Technique.ZEPPELI, 
            RewardType.ATTACK, ModHamonActions.ZEPPELI_HAMON_CUTTER,
            THROWABLES_INFUSION),
    TORNADO_OVERDRIVE("tornado_overdrive", 
            null, Technique.ZEPPELI, 
            RewardType.ATTACK, ModHamonActions.ZEPPELI_TORNADO_OVERDRIVE,
            JUMP),
    DEEP_PASS("deep_pass", 
            null, Technique.ZEPPELI, 
            RewardType.PASSIVE, null),

    CLACKER_VOLLEY("clacker_volley", 
            null, Technique.JOSEPH, 
            RewardType.ITEM, null,
            THROWABLES_INFUSION),
    ROPE_TRAP("rope_trap", 
            null, Technique.JOSEPH, 
            RewardType.ITEM, null,
            SENDO_OVERDRIVE),
    CHEAT_DEATH("cheat_death", 
            null, Technique.JOSEPH, 
            RewardType.PASSIVE, null,
            ROPE_TRAP),
    
    BUBBLE_LAUNCHER("bubble_launcher", 
            null, Technique.CAESAR, 
            RewardType.ATTACK, ModHamonActions.CAESAR_BUBBLE_LAUNCHER,
            THROWABLES_INFUSION),
    BUBBLE_CUTTER("bubble_cutter", 
            null, Technique.CAESAR, 
            RewardType.ATTACK, ModHamonActions.CAESAR_BUBBLE_CUTTER,
            BUBBLE_LAUNCHER),
    CRIMSON_BUBBLE("crimson_bubble", 
            null, Technique.CAESAR, 
            RewardType.PASSIVE, null),

    AJA_STONE_KEEPER("aja_stone_keeper", 
            null, Technique.LISA_LISA, 
            RewardType.ITEM, null),
    SATIPOROJA_SCARF("satiporoja_scarf", 
            null, Technique.LISA_LISA, 
            RewardType.ITEM, null,
            PLANT_INFUSION),
    SNAKE_MUFFLER("snake_muffler", 
            null, Technique.LISA_LISA, 
            RewardType.ITEM, null,
            DETECTOR);
    
    public static final Map<HamonSkill, ResourceLocation> iconTextureMap = Util.make(Maps.newEnumMap(HamonSkill.class), (map) -> {
        for (HamonSkill skill : HamonSkill.values()) {
            map.put(skill, new ResourceLocation(JojoMod.MOD_ID, skill.name));
        }
    });
    
    private final String name;
    private final List<HamonSkill> parentSkills = new ArrayList<>();
    @Nullable
    private final HamonStat stat;
    @Nullable
    private final Technique technique;
    private final RewardType rewardType;
    @Nullable
    private final Supplier<HamonAction> rewardAction;
    
    private HamonSkill(String name, HamonStat stat, Technique technique, RewardType rewardType, Supplier<HamonAction> rewardAction, HamonSkill... parentSkills) {
        this.name = name;
        this.rewardType = rewardType;
        this.rewardAction = rewardAction;
        this.stat = stat;
        this.technique = technique;
        Collections.addAll(this.parentSkills, parentSkills);
        if (technique != null) {
            technique.addSkill(this);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<HamonSkill> getRequiredSkills() {
        return parentSkills;
    }
    
    public RewardType getRewardType() {
        return rewardType;
    }

    @Nullable
    public HamonAction getRewardAction() {
        return rewardAction != null ? rewardAction.get() : null;
    }

    @Nullable
    public HamonStat getStat() {
        return stat;
    }

    @Nullable
    public Technique getTechnique() {
        return technique;
    }
    
    public boolean isBaseSkill() {
        return stat != null;
    }
    
    public boolean requiresTeacher() {
        return isBaseSkill();
    }
    
    public boolean isUnlockedByDefault() {
        return this == HamonSkill.OVERDRIVE || this == HamonSkill.HEALING;
    }
    
    public HamonSkillType getSkillType() {
        if (getTechnique() != null) {
            return HamonSkillType.TECHNIQUE;
        }
        return getStat() == HamonStat.STRENGTH ? HamonSkillType.STRENGTH : HamonSkillType.CONTROL;
    }
    
    public enum RewardType {
        ATTACK("attack", ActionType.ATTACK),
        ABILITY("ability", ActionType.ATTACK),
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
    
    public enum HamonStat {
        STRENGTH,
        CONTROL
    }
    
    public enum Technique {
        JONATHAN,
        ZEPPELI,
        JOSEPH,
        CAESAR,
        LISA_LISA;
        
        private final List<HamonSkill> skills = new ArrayList<>();
        
        private void addSkill(HamonSkill skill) {
            this.skills.add(skill);
        }
        
        public List<HamonSkill> getSkills() {
            return skills;
        }
    }

    public enum HamonSkillType {
        STRENGTH,
        CONTROL,
        TECHNIQUE
    }
}
