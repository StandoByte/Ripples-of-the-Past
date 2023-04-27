package com.github.standobyte.jojo.init.power;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.power.IPowerType;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class CustomRegistryHolder<V extends IForgeRegistryEntry<V>> {
    private final DeferredRegister<V> deferredRegister;
    private final String name;
    private Supplier<IForgeRegistry<V>> registrySupplier = null;
    
    public CustomRegistryHolder(DeferredRegister<V> deferredRegister, String name) {
        this.deferredRegister = deferredRegister;
        this.name = name;
    }
    
    public <I extends V> RegistryObject<I> registerEntry(String name, Supplier<? extends I> sup) {
        return deferredRegister.register(name, sup);
    }
    
    public void initRegister(IEventBus modEventBus) {
        if (registrySupplier == null) {
            registrySupplier = deferredRegister.makeRegistry(name, () -> new RegistryBuilder<>());
            deferredRegister.register(modEventBus);
        }
    }
    
    public IForgeRegistry<V> getRegistry() {
        return registrySupplier.get();
    }
    
    @Nonnull
    public String getKeyAsString(V powerType) {
        ResourceLocation resourceLocation = getRegistry().getKey(powerType);
        if (resourceLocation == null) {
           return IPowerType.NO_POWER_NAME;
        }
        return resourceLocation.toString();
    }
    
    public int getNumericId(ResourceLocation regName) {
        return ((ForgeRegistry<V>) getRegistry()).getID(regName);
    }
    
}
