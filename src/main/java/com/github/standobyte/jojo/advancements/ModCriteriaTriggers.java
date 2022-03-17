package com.github.standobyte.jojo.advancements;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.advancements.criterion.ActionPerformTrigger;
import com.github.standobyte.jojo.advancements.criterion.GetPowerTrigger;
import com.github.standobyte.jojo.advancements.criterion.HamonStatsTrigger;
import com.github.standobyte.jojo.advancements.criterion.KilledPowerUserTrigger;
import com.github.standobyte.jojo.advancements.criterion.LastHamonTrigger;
import com.github.standobyte.jojo.advancements.criterion.SoulAscensionTrigger;
import com.github.standobyte.jojo.advancements.criterion.StandArrowHitTrigger;
import com.github.standobyte.jojo.advancements.criterion.StoneMaskDestroyedTrigger;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCriteriaTriggers {
    
    public static final CriteriaTriggerSupplier<ActionPerformTrigger> ACTION_PERFORM = 
            new CriteriaTriggerSupplier<>(() -> new ActionPerformTrigger(new ResourceLocation(JojoMod.MOD_ID, "action_perform")));
    public static final CriteriaTriggerSupplier<GetPowerTrigger> GET_POWER = 
            new CriteriaTriggerSupplier<>(() -> new GetPowerTrigger(new ResourceLocation(JojoMod.MOD_ID, "get_power")));
    public static final CriteriaTriggerSupplier<LastHamonTrigger> LAST_HAMON = 
            new CriteriaTriggerSupplier<>(() -> new LastHamonTrigger(new ResourceLocation(JojoMod.MOD_ID, "last_hamon")));
    public static final CriteriaTriggerSupplier<HamonStatsTrigger> HAMON_STATS = 
            new CriteriaTriggerSupplier<>(() -> new HamonStatsTrigger(new ResourceLocation(JojoMod.MOD_ID, "hamon_stats")));
    public static final CriteriaTriggerSupplier<KilledPowerUserTrigger> PLAYER_KILLED_ENTITY = 
            new CriteriaTriggerSupplier<>(() -> new KilledPowerUserTrigger(new ResourceLocation(JojoMod.MOD_ID, "player_killed_entity"), false));
    public static final CriteriaTriggerSupplier<KilledPowerUserTrigger> ENTITY_KILLED_PLAYER = 
            new CriteriaTriggerSupplier<>(() -> new KilledPowerUserTrigger(new ResourceLocation(JojoMod.MOD_ID, "entity_killed_player"), true));
    public static final CriteriaTriggerSupplier<StoneMaskDestroyedTrigger> STONE_MASK_DESTROYED = 
            new CriteriaTriggerSupplier<>(() -> new StoneMaskDestroyedTrigger(new ResourceLocation(JojoMod.MOD_ID, "destroy_stone_mask")));
    public static final CriteriaTriggerSupplier<StandArrowHitTrigger> STAND_ARROW_HIT = 
            new CriteriaTriggerSupplier<>(() -> new StandArrowHitTrigger(new ResourceLocation(JojoMod.MOD_ID, "stand_arrow_hit")));
    public static final CriteriaTriggerSupplier<SoulAscensionTrigger> SOUL_ASCENSION = 
            new CriteriaTriggerSupplier<>(() -> new SoulAscensionTrigger(new ResourceLocation(JojoMod.MOD_ID, "soul_ascension")));
    
    
    
    public static class CriteriaTriggerSupplier<T extends AbstractCriterionTrigger<?>> implements Supplier<T> {
        private static final Set<CriteriaTriggerSupplier<?>> TO_REGISTER = new HashSet<>();
        
        private final Supplier<T> supplier;
        private T criteriaTrigger = null;
        
        private CriteriaTriggerSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
            TO_REGISTER.add(this);
        }

        @Override
        public T get() {
            return criteriaTrigger;
        }
        
        private T register() {
            T trigger = supplier.get();
            this.criteriaTrigger = trigger;
            return criteriaTrigger;
        }
        
        public static void registerAll() {
            for (CriteriaTriggerSupplier<?> supplier : TO_REGISTER) {
                CriteriaTriggers.register(supplier.register());
            }
        }
    }
}
