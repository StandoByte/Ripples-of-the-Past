package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.action.non_stand.VampirismAction;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.nonstand.type.VampirismPowerType;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class ModNonStandPowers {
    public static final DeferredRegister<NonStandPowerType<?>> POWERS = DeferredRegister.create(
            (Class<NonStandPowerType<?>>) ((Class<?>) NonStandPowerType.class), JojoMod.MOD_ID);
    
    public static final RegistryObject<VampirismPowerType> VAMPIRISM = POWERS.register("vampirism", 
            () -> new VampirismPowerType(
                    0xFF0000, 
                    new VampirismAction[] {ModActions.VAMPIRISM_BLOOD_DRAIN.get(), ModActions.VAMPIRISM_FREEZE.get(), ModActions.VAMPIRISM_SPACE_RIPPER_STINGY_EYES.get()}, 
                    new VampirismAction[] {ModActions.VAMPIRISM_BLOOD_GIFT.get(), ModActions.VAMPIRISM_ZOMBIE_SUMMON.get(), ModActions.VAMPIRISM_DARK_AURA.get()}
                    ));
    
    public static final RegistryObject<HamonPowerType> HAMON = POWERS.register("hamon", 
            () -> new HamonPowerType(
                    0xFFFF00, 
                    new HamonAction[] {ModActions.HAMON_SENDO_OVERDRIVE.get(), ModActions.HAMON_PLANT_INFUSION.get(), ModActions.HAMON_ZOOM_PUNCH.get()}, 
                    new HamonAction[] {ModActions.HAMON_HEALING.get(), ModActions.HAMON_SPEED_BOOST.get(), ModActions.HAMON_WALL_CLIMBING.get(), 
                            ModActions.HAMON_DETECTOR.get(), ModActions.HAMON_LIFE_MAGNETISM.get(), ModActions.HAMON_PROJECTILE_SHIELD.get(), ModActions.HAMON_REPELLING_OVERDRIVE.get()}
                    ));
    
    
    
    public static class Registry {
        private static Supplier<IForgeRegistry<NonStandPowerType<?>>> REGISTRY_SUPPLIER = null;
        
        public static void initRegistry() {
            if (REGISTRY_SUPPLIER == null) {
                REGISTRY_SUPPLIER = ModNonStandPowers.POWERS.makeRegistry("non_stand_type", () -> new RegistryBuilder<>());
            }
        }
        
        public static IForgeRegistry<NonStandPowerType<?>> getRegistry() {
            return REGISTRY_SUPPLIER.get();
        }
        
        @Nonnull
        public static String getKeyAsString(NonStandPowerType<?> powerType) {
            ResourceLocation resourceLocation = getRegistry().getKey(powerType);
            if (resourceLocation == null) {
               return IPowerType.NO_POWER_NAME;
            }
            return resourceLocation.toString();
        }
    }
}
