package com.github.standobyte.jojo.action.stand.effect;

import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class StandEffectType<T extends StandEffectInstance> extends ForgeRegistryEntry<StandEffectType<?>> {
    private IFactory<T> factory;
    
    public StandEffectType(IFactory<T> factory) {
        this.factory = factory;
    }
    
    @Deprecated
    public T create() {
        return create(null);
    }
    
    public T create(World world) {
        T effect = factory.create(this);
        effect.world = world;
        return effect;
    }
    
    
    
    public interface IFactory<T extends StandEffectInstance> {
        T create(StandEffectType<T> effect);
    }
}
