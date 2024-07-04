package com.github.standobyte.jojo.action.stand.effect;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class StandEffectType<T extends StandEffectInstance> extends ForgeRegistryEntry<StandEffectType<?>> {
    private IFactory<T> factory;
    
    public StandEffectType(IFactory<T> factory) {
        this.factory = factory;
    }
    
    public T create() {
        return factory.create(this);
    }
    
    
    
    public interface IFactory<T extends StandEffectInstance> {
        T create(StandEffectType<T> effect);
    }
}
