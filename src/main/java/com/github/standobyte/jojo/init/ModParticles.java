package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, JojoMod.MOD_ID);
    
    public static final RegistryObject<BasicParticleType> BLOOD = PARTICLES.register("blood", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> HAMON_SPARK = PARTICLES.register("hamon_spark", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_SPARK_BLUE = PARTICLES.register("hamon_spark_blue", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_SPARK_YELLOW = PARTICLES.register("hamon_spark_yellow", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_SPARK_RED = PARTICLES.register("hamon_spark_red", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_SPARK_SILVER = PARTICLES.register("hamon_spark_silver", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> HAMON_AURA = PARTICLES.register("hamon_aura", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_AURA_BLUE = PARTICLES.register("hamon_aura_blue", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_AURA_YELLOW = PARTICLES.register("hamon_aura_yellow", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_AURA_RED = PARTICLES.register("hamon_aura_red", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> HAMON_AURA_SILVER = PARTICLES.register("hamon_aura_silver", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> BOILING_BLOOD_POP = PARTICLES.register("boiling_blood", () -> new BasicParticleType(false));

    public static final RegistryObject<BasicParticleType> METEORITE_VIRUS = PARTICLES.register("meteorite_virus", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> MENACING = PARTICLES.register("menacing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> RESOLVE = PARTICLES.register("resolve", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> SOUL_CLOUD = PARTICLES.register("soul_cloud", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> AIR_STREAM = PARTICLES.register("air_stream", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> FLAME_ONE_TICK = PARTICLES.register("flame", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> CD_RESTORATION = PARTICLES.register("cd_restoration", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> RPS_ROCK = PARTICLES.register("rps_rock", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> RPS_PAPER = PARTICLES.register("rps_paper", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> RPS_SCISSORS = PARTICLES.register("rps_scissors", () -> new BasicParticleType(false));
}
