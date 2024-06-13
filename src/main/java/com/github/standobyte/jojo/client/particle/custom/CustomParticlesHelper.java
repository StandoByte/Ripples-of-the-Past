package com.github.standobyte.jojo.client.particle.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.HamonAuraParticle;
import com.github.standobyte.jojo.client.particle.SendoHamonOverdriveParticle;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomParticlesHelper {
    
    private static final Map<ResourceLocation, IAnimatedSprite> SPRITE_SETS = new HashMap<>();
    
    public static void saveSprites(Minecraft mc) {
        Map<ResourceLocation, ? extends IAnimatedSprite> spritesMap = ClientReflection.getSpriteSets(mc.particleEngine);
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.CD_RESTORATION.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_BLUE.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_YELLOW.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_RED.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_SILVER.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_BLUE.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_YELLOW.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_RED.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_SILVER.get());
    }
    
    public static IAnimatedSprite getSavedSpriteSet(ParticleType<?> particleType) {
        return SPRITE_SETS.get(ForgeRegistries.PARTICLE_TYPES.getKey(particleType));
    }
    
    private static void saveSprites(Map<ResourceLocation, ? extends IAnimatedSprite> sprites, ParticleType<?> particleType) {
        ResourceLocation key = ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
        SPRITE_SETS.put(key, sprites.get(key));
    }
    
    
    
    public static boolean createCDRestorationParticle(LivingEntity entity, Hand hand) {
        if (!ClientUtil.canSeeStands()) return false;
        
        EntityPosParticle particle = CDRestorationHandItemParticle.createCustomParticle((ClientWorld) entity.level, entity, hand);
        return addParticle(particle, particle.getPos(), false, false);
    }
    
    public static boolean createBloodParticle(IParticleData type, @Nullable Entity entity, 
            double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        BloodFromEntityParticle particle = BloodFromEntityParticle.createCustomParticle(
                type, Minecraft.getInstance().level, entity, x, y, z, xSpeed, ySpeed, zSpeed);
        return addParticle(particle, new Vector3d(x, y, z), false, false);
    }
    
    public static void createHamonAuraParticle(IParticleData type, 
            LivingEntity user, double x, double y, double z) {
        IAnimatedSprite sprite = getSavedSpriteSet(type.getType());
        if (sprite != null) {
            HamonAuraParticle particle = HamonAura3PersonParticle.createCustomParticle(
                    sprite, Minecraft.getInstance().level, user, x, y, z);
            addParticle(particle, new Vector3d(x, y, z), false, false);
        }
        else {
            Minecraft.getInstance().level.addParticle(type, x, y, z, 0, 0, 0);
        }
    }
    
    // FIXME !!!!!! particles at arms in 1st person
    public static void summonHamonAuraParticlesFirstPerson(IParticleData type, LivingEntity user, float particlesPerTick) {
        IAnimatedSprite sprite = getSavedSpriteSet(type.getType());
        if (sprite != null) {
            Random random = user.getRandom();
            FirstPersonHamonAura particles = FirstPersonHamonAura.getInstance();
            
            for (HandSide handSide : HandSide.values()) {
                GeneralUtil.doFractionTimes(() -> {
//                    double x = random.nextDouble() * 0.25 - 0.5;   // -0.5 - -0.25
//                    double y = random.nextDouble() * 0.75 + 0.125; // 0.125 - 0.875
//                    double z = random.nextDouble() * 0.25 - 0.125; // -0.125 - 0.125
                    double x = random.nextDouble() * 0.5 - 0.625;
                    double y = random.nextDouble();
                    double z = random.nextDouble() * 0.5 - 0.25;
                    if (handSide == HandSide.LEFT) {
                        x = -x;
                    }
                    
                    particles.add(new FirstPersonHamonAura.HamonAuraPseudoParticle(x, y, z, sprite, handSide));
                }, particlesPerTick);
            }
        }
    }
    
    public static void addSendoHamonOverdriveParticle(World level, IParticleData pParticleData, Direction.Axis blockAxis, 
            double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, int lifeTime) {
        SpriteTexturedParticle particle = new SendoHamonOverdriveParticle(
                (ClientWorld) level, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, blockAxis);
        particle.setLifetime(lifeTime);
        particle.pickSprite(getSavedSpriteSet(pParticleData.getType()));
        particle.setColor(1, 1, 1);
        addParticle(particle, new Vector3d(pX, pY, pZ), pParticleData.getType().getOverrideLimiter(), false);
    }
    
    public static void createHamonGliderChargeParticles(LivingEntity entity) {
        EntityPosParticle particleLeft = HamonGliderChargingParticle.createCustomParticle((ClientWorld) entity.level, entity, Hand.MAIN_HAND);
        EntityPosParticle particleRight = HamonGliderChargingParticle.createCustomParticle((ClientWorld) entity.level, entity, Hand.OFF_HAND);
        addParticle(particleLeft, particleLeft.getPos(), false, false);
        addParticle(particleRight, particleRight.getPos(), false, false);
    }
    
    public static void createHamonSparkParticles(@Nullable Entity entityToFollow, Vector3d pos, int particlesCount) {
        createHamonSparkParticles(entityToFollow, pos.x, pos.y, pos.z, particlesCount);
    }
    
    private static final Random RANDOM = new Random();
    private static final double SPARK_PARTICLE_DIST = 0.05;
    private static final double SPARK_PARTICLE_SPEED = 0.25;
    public static void createHamonSparkParticles(@Nullable Entity entityToFollow, double x, double y, double z, int particlesCount) {
        Minecraft mc = Minecraft.getInstance();
        IParticleData particleData = ModParticles.HAMON_SPARK.get();
        for (int i = 0; i < particlesCount; ++i) {
            double xOffset = RANDOM.nextGaussian() * SPARK_PARTICLE_DIST;
            double yOffset = RANDOM.nextGaussian() * SPARK_PARTICLE_DIST;
            double zOffset = RANDOM.nextGaussian() * SPARK_PARTICLE_DIST;
            double xSpeed = RANDOM.nextGaussian() * SPARK_PARTICLE_SPEED;
            double ySpeed = RANDOM.nextGaussian() * SPARK_PARTICLE_SPEED;
            double zSpeed = RANDOM.nextGaussian() * SPARK_PARTICLE_SPEED;
            
            if (entityToFollow == null) {
                mc.level.addParticle(particleData, false, x + xOffset, y + yOffset, z + zOffset, xSpeed, ySpeed, zSpeed);
            }
            else {
                Particle particle = new HamonSparkEntityOffsetParticle(mc.level, entityToFollow, 
                        x, y, z, xSpeed, ySpeed, zSpeed, particleData.getType());
                CustomParticlesHelper.addParticle(particle, new Vector3d(x, y, z), particleData.getType().getOverrideLimiter(), false);
            }
        }
    }
    
    public static void createParticlesEmitter(Entity entity, IParticleData type, int ticks) {
        Minecraft.getInstance().particleEngine.createTrackingEmitter(entity, type, ticks);
    }
    
    public static boolean addParticle(Particle particle, Vector3d particlePos, boolean overrideLimiter, boolean alwaysVisible) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo activerenderinfo = mc.gameRenderer.getMainCamera();
        if (activerenderinfo.isInitialized() && mc.particleEngine != null && activerenderinfo.getPosition().distanceToSqr(particlePos) < 1024.0D) {
            if (alwaysVisible || calculateParticleLevel(mc, mc.level, alwaysVisible) != ParticleStatus.MINIMAL) {
                mc.particleEngine.add(particle);
                return true;
            } 
        }
        return false;
    }
    
    private static ParticleStatus calculateParticleLevel(Minecraft mc, ClientWorld world, boolean overrideLimiter) {
        ParticleStatus status = mc.options.particles;
        if (overrideLimiter && status == ParticleStatus.MINIMAL && world.random.nextInt(10) == 0) {
            status = ParticleStatus.DECREASED;
        }

        if (status == ParticleStatus.DECREASED && world.random.nextInt(3) == 0) {
            status = ParticleStatus.MINIMAL;
        }

        return status;
    }
}
