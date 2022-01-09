package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, JojoMod.MOD_ID);
    
    public static final RegistryObject<BasicParticleType> HAMON_SPARK = PARTICLES.register("hamon_spark", 
            () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> METEORITE_VIRUS = PARTICLES.register("meteorite_virus", 
            () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> MENACING = PARTICLES.register("menacing", 
            () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> RESOLVE = PARTICLES.register("resolve", 
            () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> SOUL_CLOUD = PARTICLES.register("soul_cloud", 
            () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> FLAME_ONE_TICK = PARTICLES.register("flame", 
            () -> new BasicParticleType(false));
}
