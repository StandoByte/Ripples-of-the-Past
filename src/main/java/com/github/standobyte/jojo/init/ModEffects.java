package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.potion.FreezeEffect;
import com.github.standobyte.jojo.potion.HamonSpreadEffect;
import com.github.standobyte.jojo.potion.StaminaRegenEffect;
import com.github.standobyte.jojo.potion.StatusEffect;
import com.github.standobyte.jojo.potion.StunEffect;
import com.github.standobyte.jojo.potion.UncurableEffect;
import com.github.standobyte.jojo.potion.UndeadRegenerationEffect;
import com.github.standobyte.jojo.power.nonstand.type.VampirismPowerType;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, JojoMod.MOD_ID);
    
    public static final RegistryObject<FreezeEffect> FREEZE = EFFECTS.register("freeze", 
            () -> new FreezeEffect(EffectType.HARMFUL, 0xD6D6FF));
    
    public static final RegistryObject<UndeadRegenerationEffect> UNDEAD_REGENERATION = EFFECTS.register("undead_regeneration", 
            () -> new UndeadRegenerationEffect(EffectType.BENEFICIAL, Effects.REGENERATION.getColor()));
    
    public static final RegistryObject<HamonSpreadEffect> HAMON_SPREAD = EFFECTS.register("hamon_spread", 
            () -> new HamonSpreadEffect(EffectType.HARMFUL, 0xFFC10A));
    
    public static final RegistryObject<StunEffect> STUN = EFFECTS.register("stun", 
            () -> new StunEffect(EffectType.HARMFUL, 0x404040));
    
    public static final RegistryObject<UncurableEffect> MEDITATION = EFFECTS.register("meditation", 
            () -> new UncurableEffect(EffectType.NEUTRAL, 0xDD6000));
    
    public static final RegistryObject<UncurableEffect> CHEAT_DEATH = EFFECTS.register("cheat_death", 
            () -> new UncurableEffect(EffectType.BENEFICIAL, 0xEADB84));
    
    public static final RegistryObject<StaminaRegenEffect> STAMINA_REGEN = EFFECTS.register("stamina_regen", 
            () -> new StaminaRegenEffect(EffectType.BENEFICIAL, 0x149900));
    
    public static final RegistryObject<UncurableEffect> TIME_STOP = EFFECTS.register("time_stop", 
            () -> new UncurableEffect(EffectType.BENEFICIAL, 0x707070));
    
    public static final RegistryObject<Effect> SUN_RESISTANCE = EFFECTS.register("sun_resistance", 
            () -> new StatusEffect(EffectType.BENEFICIAL, 0xFFD54A));
    
//    public static final RegistryObject<Effect> STAND_SEALING = EFFECTS.register("stand_sealing", 
//            () -> new StatusEffect(EffectType.HARMFUL, 0xCACAD8)); // TODO Stand Sealing effect
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void afterEffectsRegister(RegistryEvent.Register<Effect> event) {
        VampirismPowerType.initVampiricEffectsMap();
        StandEntity.addSharedEffects(TIME_STOP.get(), Effects.BLINDNESS);
    }
}
