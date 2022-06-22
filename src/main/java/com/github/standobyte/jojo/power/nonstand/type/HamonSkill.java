package com.github.standobyte.jojo.power.nonstand.type;

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.actions.HamonAction;
import com.github.standobyte.jojo.init.ModActions;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public enum HamonSkill {
    OVERDRIVE("overdrive", null, null, 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    SENDO_OVERDRIVE("sendo_overdrive", OVERDRIVE, null, 
            HamonStat.STRENGTH, null, 
            RewardType.ATTACK, ModActions.HAMON_SENDO_OVERDRIVE),
    TURQUOISE_BLUE_OVERDRIVE("turquoise_blue_overdrive", OVERDRIVE, null, 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    SUNLIGHT_YELLOW_OVERDRIVE("sunlight_yellow_overdrive", SENDO_OVERDRIVE, TURQUOISE_BLUE_OVERDRIVE, 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    
    PLANT_INFUSION("plant_infusion", null, null, 
            HamonStat.STRENGTH, null, 
            RewardType.ATTACK, ModActions.HAMON_PLANT_INFUSION),
    THROWABLES_INFUSION("throwables_infusion", PLANT_INFUSION, null, 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    ANIMAL_INFUSION("animal_infusion", PLANT_INFUSION, null, 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, ModActions.HAMON_ORGANISM_INFUSION),
    ARROW_INFUSION("arrow_infusion", THROWABLES_INFUSION, ANIMAL_INFUSION, 
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    
    ZOOM_PUNCH("zoom_punch", null, null, 
            HamonStat.STRENGTH, null, 
            RewardType.ATTACK, ModActions.HAMON_ZOOM_PUNCH),
    JUMP("jump", ZOOM_PUNCH, null, 
            HamonStat.STRENGTH, null, 
            RewardType.ABILITY, null),
    SPEED_BOOST("speed_boost", ZOOM_PUNCH, null, 
            HamonStat.STRENGTH, null, 
            RewardType.ABILITY, ModActions.HAMON_SPEED_BOOST),
    AFTERIMAGES("afterimages", JUMP, SPEED_BOOST,
            HamonStat.STRENGTH, null, 
            RewardType.PASSIVE, null),
    
    HEALING("healing", null, null, 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModActions.HAMON_HEALING),
    PLANTS_GROWTH("plants_growth", HEALING, null, 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null),
    EXPEL_VENOM("expel_venom", HEALING, null, 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null),
    HEALING_TOUCH("healing_touch", PLANTS_GROWTH, EXPEL_VENOM, 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null),

    WALL_CLIMBING("wall_climbing", null, null, 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModActions.HAMON_WALL_CLIMBING),
    DETECTOR("detector", WALL_CLIMBING, null, 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModActions.HAMON_DETECTOR),
    LIFE_MAGNETISM("life_magnetism", WALL_CLIMBING, null, 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModActions.HAMON_LIFE_MAGNETISM),
    HAMON_SPREAD("hamon_spread", DETECTOR, LIFE_MAGNETISM, 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null),

    REPELLING_OVERDRIVE("repelling_overdrive", null, null, 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModActions.HAMON_REPELLING_OVERDRIVE),
    PROJECTILE_SHIELD("projectile_shield", REPELLING_OVERDRIVE, null, 
            HamonStat.CONTROL, null, 
            RewardType.ABILITY, ModActions.HAMON_PROJECTILE_SHIELD),
    WATER_WALKING("water_walking", REPELLING_OVERDRIVE, null, 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null), // FIXME liquid walking (water)
    LAVA_WALKING("lava_walking", REPELLING_OVERDRIVE, WATER_WALKING, 
            HamonStat.CONTROL, null, 
            RewardType.PASSIVE, null), // FIXME liquid walking (lava)
    

    NATURAL_TALENT("natural_talent", null, null, 
            null, Technique.JONATHAN, 
            RewardType.ATTACK, ModActions.JONATHAN_OVERDRIVE_BARRAGE),
    SCARLET_OVERDRIVE("scarlet_overdrive", NATURAL_TALENT, null, 
            null, Technique.JONATHAN, 
            RewardType.ATTACK, ModActions.JONATHAN_SCARLET_OVERDRIVE),
    METAL_SILVER_OVERDRIVE("metal_silver_overdrive", SCARLET_OVERDRIVE, null, 
            null, Technique.JONATHAN, 
            RewardType.PASSIVE, null),
    
    HAMON_CUTTER("hamon_cutter", null, null, 
            null, Technique.ZEPPELI, 
            RewardType.ATTACK, ModActions.ZEPPELI_HAMON_CUTTER),
    TORNADO_OVERDRIVE("tornado_overdrive", HAMON_CUTTER, null, 
            null, Technique.ZEPPELI, 
            RewardType.ATTACK, ModActions.ZEPPELI_TORNADO_OVERDRIVE),
    DEEP_PASS("deep_pass", TORNADO_OVERDRIVE, null, 
            null, Technique.ZEPPELI, 
            RewardType.PASSIVE, null),

    CLACKER_VOLLEY("clacker_volley", null, null, 
            null, Technique.JOSEPH, 
            RewardType.ITEM, null),
    ROPE_TRAP("rope_trap", CLACKER_VOLLEY, null, 
            null, Technique.JOSEPH, 
            RewardType.ITEM, null),
    CHEAT_DEATH("cheat_death", ROPE_TRAP, null, 
            null, Technique.JOSEPH, 
            RewardType.PASSIVE, null),
    
    BUBBLE_LAUNCHER("bubble_launcher", null, null, 
            null, Technique.CAESAR, 
            RewardType.ATTACK, ModActions.CAESAR_BUBBLE_LAUNCHER),
    BUBBLE_CUTTER("bubble_cutter", BUBBLE_LAUNCHER, null, 
            null, Technique.CAESAR, 
            RewardType.ATTACK, ModActions.CAESAR_BUBBLE_CUTTER),
    CRIMSON_BUBBLE("crimson_bubble", BUBBLE_CUTTER, null, 
            null, Technique.CAESAR, 
            RewardType.PASSIVE, null),

    AJA_STONE_KEEPER("aja_stone_keeper", null, null, 
            null, Technique.LISA_LISA, 
            RewardType.ITEM, null),
    SATIPOROJA_SCARF("satiporoja_scarf", AJA_STONE_KEEPER, null, 
            null, Technique.LISA_LISA, 
            RewardType.ITEM, null),
    SNAKE_MUFFLER("snake_muffler", SATIPOROJA_SCARF, null, 
            null, Technique.LISA_LISA, 
            RewardType.ITEM, null);
    
    public static final Map<HamonSkill, ResourceLocation> iconTextureMap = Util.make(Maps.newEnumMap(HamonSkill.class), (map) -> {
        for (HamonSkill skill : HamonSkill.values()) {
            map.put(skill, new ResourceLocation(JojoMod.MOD_ID, skill.name));
        }
    });
    
    private final String name;
    @Nullable
    private final HamonSkill parent1;
    @Nullable
    private final HamonSkill parent2;
    @Nullable
    private final HamonStat stat;
    @Nullable
    private final Technique technique;
    private final RewardType rewardType;
    @Nullable
    private final Supplier<HamonAction> rewardAction;
    
    private HamonSkill(String name, HamonSkill parent1, HamonSkill parent2, HamonStat stat, Technique technique, RewardType rewardType, Supplier<HamonAction> rewardAction) {
        this.name = name;
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.rewardType = rewardType;
        this.rewardAction = rewardAction;
        this.stat = stat;
        this.technique = technique;
    }
    
    public String getName() {
        return this.name;
    }

    @Nullable
    public HamonSkill getParent1() {
        return parent1;
    }

    @Nullable
    public HamonSkill getParent2() {
        return parent2;
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
    
    public boolean requiresTeacher() {
        return stat != null;
    }
    
    public HamonSkillType getSkillType() {
        if (getTechnique() != null) {
            return HamonSkillType.TECHNIQUE;
        }
        return getStat() == HamonStat.STRENGTH ? HamonSkillType.STRENGTH : HamonSkillType.CONTROL;
    }
    
    public enum RewardType {
        ATTACK("attack"),
        ABILITY("ability"),
        PASSIVE("passive"),
        ITEM("item");
        
        private final ITextComponent name;
        
        private RewardType(String key) {
            this.name = new TranslationTextComponent("hamon.skill_type." + key).withStyle(TextFormatting.ITALIC);
        }
        
        public ITextComponent getName() {
            return name;
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
    }

    public enum HamonSkillType {
        STRENGTH,
        CONTROL,
        TECHNIQUE
    }
}
