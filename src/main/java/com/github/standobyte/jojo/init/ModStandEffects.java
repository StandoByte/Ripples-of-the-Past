package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.effect.BoyIIManStandPartTakenEffect;
import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks;
import com.github.standobyte.jojo.action.stand.effect.DriedBloodDrops;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.power.IPowerType;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModStandEffects {
    public static final DeferredRegister<StandEffectType<?>> STAND_EFFECTS = DeferredRegister.create(
            (Class<StandEffectType<?>>) ((Class<?>) StandEffectType.class), JojoMod.MOD_ID);
    
    public static final RegistryObject<StandEffectType<BoyIIManStandPartTakenEffect>> BOY_II_MAN_PART_TAKE = STAND_EFFECTS.register("boy_ii_man_part_take", 
            () -> new StandEffectType<>(BoyIIManStandPartTakenEffect::new));
    
    public static final RegistryObject<StandEffectType<CrazyDiamondRestorableBlocks>> CRAZY_DIAMOND_RESTORABLE_BLOCKS = STAND_EFFECTS.register("crazy_diamond_restorable_blocks", 
            () -> new StandEffectType<>(CrazyDiamondRestorableBlocks::new));
    
    public static final RegistryObject<StandEffectType<DriedBloodDrops>> DRIED_BLOOD_DROPS = STAND_EFFECTS.register("dried_blood_drops", 
            () -> new StandEffectType<>(DriedBloodDrops::new));
    
    
    
    public static class Registry {
        private static Supplier<IForgeRegistry<StandEffectType<?>>> REGISTRY_SUPPLIER = null;
        
        public static void initRegistry() {
            if (REGISTRY_SUPPLIER == null) {
                REGISTRY_SUPPLIER = ModStandEffects.STAND_EFFECTS.makeRegistry("stand_effect", () -> new RegistryBuilder<>());
            }
        }
        
        public static IForgeRegistry<StandEffectType<?>> getRegistry() {
            return REGISTRY_SUPPLIER.get();
        }
        
        @Nonnull
        public static String getKeyAsString(StandEffectType<?> standType) {
            ResourceLocation resourceLocation = getRegistry().getKey(standType);
            if (resourceLocation == null) {
               return IPowerType.NO_POWER_NAME;
            }
            return resourceLocation.toString();
        }
        
        public static int getNumericId(ResourceLocation regName) {
            return ((ForgeRegistry<StandEffectType<?>>) getRegistry()).getID(regName);
        }
    }
}
