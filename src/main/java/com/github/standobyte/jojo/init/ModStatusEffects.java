package com.github.standobyte.jojo.init;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.potion.FreezeEffect;
import com.github.standobyte.jojo.potion.HamonShockEffect;
import com.github.standobyte.jojo.potion.HamonSpreadEffect;
import com.github.standobyte.jojo.potion.HypnosisEffect;
import com.github.standobyte.jojo.potion.ImmobilizeEffect;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.potion.ResolveEffect;
import com.github.standobyte.jojo.potion.StaminaRegenEffect;
import com.github.standobyte.jojo.potion.StatusEffect;
import com.github.standobyte.jojo.potion.StunEffect;
import com.github.standobyte.jojo.potion.UncurableEffect;
import com.github.standobyte.jojo.potion.UndeadRegenerationEffect;
import com.github.standobyte.jojo.potion.VampireSunBurnEffect;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.zombie.ZombiePowerType;

import net.minecraft.entity.LivingEntity;
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
public class ModStatusEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, JojoMod.MOD_ID);
    
    // TODO update invisibility status
    public static final RegistryObject<Effect> FULL_INVISIBILITY = EFFECTS.register("full_invisibility", 
            () -> new StatusEffect(EffectType.BENEFICIAL, Effects.INVISIBILITY.getColor()));
    
    public static final RegistryObject<FreezeEffect> FREEZE = EFFECTS.register("freeze", 
            () -> new FreezeEffect(EffectType.HARMFUL, 0xD6D6FF));
    
    public static final RegistryObject<UndeadRegenerationEffect> UNDEAD_REGENERATION = EFFECTS.register("undead_regeneration", 
            () -> new UndeadRegenerationEffect(EffectType.BENEFICIAL, Effects.REGENERATION.getColor()));
    
    public static final RegistryObject<VampireSunBurnEffect> VAMPIRE_SUN_BURN = EFFECTS.register("sun_burn", 
            () -> new VampireSunBurnEffect());
    
    public static final RegistryObject<HamonSpreadEffect> HAMON_SPREAD = EFFECTS.register("hamon_spread", 
            () -> new HamonSpreadEffect(EffectType.HARMFUL, 0xFFC10A));
    
    public static final RegistryObject<StunEffect> STUN = EFFECTS.register("stun", 
            () -> new StunEffect(0x404040));
    
    public static final RegistryObject<StunEffect> HAMON_SHOCK = EFFECTS.register("hamon_shock", 
            () -> new HamonShockEffect(0xFFC10A));
    
    public static final RegistryObject<ImmobilizeEffect> IMMOBILIZE = EFFECTS.register("immobilize", 
            () -> new ImmobilizeEffect(0x404040));
    
    public static final RegistryObject<HypnosisEffect> HYPNOSIS = EFFECTS.register("hypnosis", 
            () -> new HypnosisEffect(0x998CBC));
    
    public static final RegistryObject<UncurableEffect> CHEAT_DEATH = EFFECTS.register("cheat_death", 
            () -> new UncurableEffect(EffectType.BENEFICIAL, 0xEADB84));
    
    public static final RegistryObject<StaminaRegenEffect> STAMINA_REGEN = EFFECTS.register("stamina_regen", 
            () -> new StaminaRegenEffect(EffectType.BENEFICIAL, 0x149900));

    public static final RegistryObject<UncurableEffect> TIME_STOP = EFFECTS.register("time_stop", 
            () -> new UncurableEffect(EffectType.BENEFICIAL, 0x707070));
    
    public static final RegistryObject<StandVirusEffect> STAND_VIRUS = EFFECTS.register("stand_virus", 
            () -> new StandVirusEffect(0xC10019));
    
    public static final RegistryObject<UncurableEffect> RESOLVE = EFFECTS.register("resolve", 
            () -> new ResolveEffect(EffectType.BENEFICIAL, 0xC6151F));
    
    public static final RegistryObject<Effect> SUN_RESISTANCE = EFFECTS.register("sun_resistance", 
            () -> new StatusEffect(EffectType.BENEFICIAL, 0xFFD54A));
    
    public static final RegistryObject<Effect> SPIRIT_VISION = EFFECTS.register("spirit_vision", 
            () -> new StatusEffect(EffectType.BENEFICIAL, 0x8E45FF));
    
    public static final RegistryObject<Effect> MISSHAPEN_FACE = EFFECTS.register("misshapen_face", 
            () -> new StatusEffect(EffectType.HARMFUL, 0x808080));

    public static final RegistryObject<Effect> MISSHAPEN_ARMS = EFFECTS.register("misshapen_arms", 
            () -> new StatusEffect(EffectType.HARMFUL, 0x808080));
    
    public static final RegistryObject<Effect> MISSHAPEN_LEGS = EFFECTS.register("misshapen_legs", 
            () -> new StatusEffect(EffectType.HARMFUL, 0x808080));
    
//    public static final RegistryObject<Effect> STAND_SEALING = EFFECTS.register("stand_sealing", 
//            () -> new StatusEffect(EffectType.HARMFUL, 0xCACAD8)); // TODO Stand Sealing effect
    
    private static final Set<Effect> TRACKED_EFFECTS = new HashSet<>();
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void afterEffectsRegister(RegistryEvent.Register<Effect> event) {
        VampirismPowerType.initVampiricEffects();
        ZombiePowerType.initZombieEffects();
        StandEntity.addSharedEffectsFromUser(TIME_STOP.get(), Effects.BLINDNESS);
        StandEntity.addSharedEffectsFromStand(STUN.get(), IMMOBILIZE.get());
        setEffectAsTracked(
                RESOLVE.get(), 
                TIME_STOP.get(), 
                IMMOBILIZE.get(), 
                STUN.get(), 
                HAMON_SHOCK.get(), 
                HYPNOSIS.get(), 
                HAMON_SPREAD.get(), 
                FULL_INVISIBILITY.get(), 
                VAMPIRE_SUN_BURN.get(),
        		FREEZE.get());
    }
    
    // Makes it so that the effect is also sent to the surrounding players, in case it is needed for visuals
    public static void setEffectAsTracked(Effect... effects) {
        Collections.addAll(TRACKED_EFFECTS, effects);
    }
    
    public static boolean isEffectTracked(Effect effect) {
        return TRACKED_EFFECTS.contains(effect);
    }
    
    
    
    public static boolean isStunned(LivingEntity entity) {
        return entity.hasEffect(STUN.get()) || entity.hasEffect(HAMON_SHOCK.get());
    }
}
