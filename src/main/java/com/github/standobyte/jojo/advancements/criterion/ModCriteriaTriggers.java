package com.github.standobyte.jojo.advancements.criterion;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;

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
