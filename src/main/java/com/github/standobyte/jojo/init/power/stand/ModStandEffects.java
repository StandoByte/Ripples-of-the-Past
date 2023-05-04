package com.github.standobyte.jojo.init.power.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.effect.BoyIIManStandPartTakenEffect;
import com.github.standobyte.jojo.action.stand.effect.DriedBloodDrops;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModStandEffects {
    public static final DeferredRegister<StandEffectType<?>> STAND_EFFECTS = DeferredRegister.create(
            (Class<StandEffectType<?>>) ((Class<?>) StandEffectType.class), JojoMod.MOD_ID);
    
    public static final RegistryObject<StandEffectType<BoyIIManStandPartTakenEffect>> BOY_II_MAN_PART_TAKE = STAND_EFFECTS.register("boy_ii_man_part_take", 
            () -> new StandEffectType<>(BoyIIManStandPartTakenEffect::new));
    
    public static final RegistryObject<StandEffectType<DriedBloodDrops>> DRIED_BLOOD_DROPS = STAND_EFFECTS.register("dried_blood_drops", 
            () -> new StandEffectType<>(DriedBloodDrops::new));
    
}
