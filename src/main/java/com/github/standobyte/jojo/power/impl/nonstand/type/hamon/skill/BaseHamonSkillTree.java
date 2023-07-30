package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;

public class BaseHamonSkillTree {
    private static final List<BaseHamonSkillTree> toInit = new ArrayList<>();
    
    public static final BaseHamonSkillTree OVERDRIVE = new BaseHamonSkillTree.Builder()
            .addSkill(ModHamonSkills.OVERDRIVE).endTier()
            .addSkill(ModHamonSkills.SENDO_OVERDRIVE).addSkill(ModHamonSkills.TURQUOISE_BLUE_OVERDRIVE).endTier()
            .addSkill(ModHamonSkills.SUNLIGHT_YELLOW_OVERDRIVE).build("overdrive");
    
    public static final BaseHamonSkillTree INFUSION = new BaseHamonSkillTree.Builder()
            .addSkill(ModHamonSkills.THROWABLES_INFUSION).endTier()
            .addSkill(ModHamonSkills.PLANT_BLOCK_INFUSION).addSkill(ModHamonSkills.PLANT_ITEM_INFUSION).endTier()
            .addSkill(ModHamonSkills.ANIMAL_INFUSION).addSkill(ModHamonSkills.ARROW_INFUSION).build("infusion");
    
    public static final BaseHamonSkillTree FLEXIBILITY = new BaseHamonSkillTree.Builder()
            .addSkill(ModHamonSkills.ZOOM_PUNCH).endTier()
            .addSkill(ModHamonSkills.JUMP).addSkill(ModHamonSkills.SPEED_BOOST).endTier()
            .addSkill(ModHamonSkills.AFTERIMAGES).build("flexibility");
    
    public static final BaseHamonSkillTree LIFE_ENERGY = new BaseHamonSkillTree.Builder()
            .addSkill(ModHamonSkills.HEALING).endTier()
            .addSkill(ModHamonSkills.PLANTS_GROWTH).addSkill(ModHamonSkills.EXPEL_VENOM).endTier()
            .addSkill(ModHamonSkills.HEALING_TOUCH).build("life");
    
    public static final BaseHamonSkillTree ATTRACTANT_REPELLENT = new BaseHamonSkillTree.Builder()
            .addSkill(ModHamonSkills.WALL_CLIMBING).addSkill(ModHamonSkills.LIQUID_WALKING).endTier()
            .addSkill(ModHamonSkills.LIFE_MAGNETISM).addSkill(ModHamonSkills.PROJECTILE_SHIELD).endTier()
            .addSkill(ModHamonSkills.PROTECTION).build("attractant_repellent");
    
    public static final BaseHamonSkillTree BODY_MANIPULATION = new BaseHamonSkillTree.Builder()
            .addSkill(ModHamonSkills.DETECTOR).endTier()
            .addSkill(ModHamonSkills.HYPNOSIS).addSkill(ModHamonSkills.HAMON_SHOCK).endTier()
            .addSkill(ModHamonSkills.HAMON_SPREAD).build("body_manipulation");
    
    protected List<List<? extends AbstractHamonSkill>> tiers;
    private final String name;
    
    protected BaseHamonSkillTree(Builder builder, String name) {
        this.tiersPreInit = builder.tiers;
        this.name = name;
        toInit.add(this);
    }
    
    public List<? extends AbstractHamonSkill> getTier(int tier) {
        if (tier >= 0 && tier < tiers.size()) {
            return tiers.get(tier);
        }
        return Collections.emptyList();
    }
    
    public List<List<? extends AbstractHamonSkill>> getAllTiers() {
        return tiers;
    }
    
    public String getName() {
        return name;
    }
    
    
    
    protected final List<List<Supplier<? extends AbstractHamonSkill>>> tiersPreInit;
    public static void initTrees() {
        toInit.forEach(BaseHamonSkillTree::init);
    }
    
    protected void init() {
        tiers = tiersPreInit.stream()
                .map(list -> list.stream().map(Supplier::get).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
    
    
    protected static class Builder {
        protected final List<List<Supplier<? extends AbstractHamonSkill>>> tiers = new ArrayList<>();
        protected List<Supplier<? extends AbstractHamonSkill>> skills = new ArrayList<>();
        
        protected Builder() {
            endTier();
        }
        
        protected Builder addSkill(Supplier<? extends AbstractHamonSkill> skill) {
            skills.add(skill);
            return this;
        }
        
        protected Builder endTier() { // too lazy to write an algorithm to automatically build the trees from parents
            skills = new ArrayList<>();
            tiers.add(skills);
            return this;
        }
        
        protected BaseHamonSkillTree build(String name) {
            return new BaseHamonSkillTree(this, name);
        }
    }
}
