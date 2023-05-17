package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraftforge.eventbus.api.IEventBus;

public class JojoCustomRegistries {
    
    public static final CustomRegistryHolder<Action<?>> ACTIONS = new CustomRegistryHolder<>(
            ModCommonRegisters.ACTIONS, "action");
    
    public static final CustomRegistryHolder<NonStandPowerType<?>> NON_STAND_POWERS = new CustomRegistryHolder<>(
            ModCommonRegisters.NON_STAND_POWERS, "non_stand_type");
    
    public static final CustomRegistryHolder<AbstractHamonSkill> HAMON_SKILLS = new CustomRegistryHolder<>(
            ModHamonSkills.HAMON_SKILLS, "hamon_skill");
    
    public static final CustomRegistryHolder<CharacterHamonTechnique> HAMON_CHARACTER_TECHNIQUES = new CustomRegistryHolder<>(
            ModHamonSkills.HAMON_CHARACTER_TECHNIQUES, "hamon_techniques");
    
    public static final CustomRegistryHolder<StandType<?>> STANDS = new CustomRegistryHolder<>(
            ModStandsInit.STAND_TYPES, "stand_type");
    
    public static final CustomRegistryHolder<StandEffectType<?>> STAND_EFFECTS = new CustomRegistryHolder<>(
            ModStandEffects.STAND_EFFECTS, "stand_effect");

    
    
    public static void initCustomRegistries(IEventBus modEventBus) {
        ACTIONS.initRegistry(modEventBus);
        NON_STAND_POWERS.initRegistry(modEventBus);
        HAMON_SKILLS.initRegistry(modEventBus);
        HAMON_CHARACTER_TECHNIQUES.initRegistry(modEventBus);
        STANDS.initRegistry(modEventBus);
        STAND_EFFECTS.initRegistry(modEventBus);
        
        // just for the sake of splitting the actions to different files
        // otherwise the classes with just RegistryObject instances won't load in time
        ModHamonActions.loadRegistryObjects();
        ModVampirismActions.loadRegistryObjects();
    }
}
