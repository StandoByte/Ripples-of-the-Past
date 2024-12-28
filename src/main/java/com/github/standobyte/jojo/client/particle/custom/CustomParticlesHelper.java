package com.github.standobyte.jojo.client.particle.custom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.HamonAuraParticle;
import com.github.standobyte.jojo.client.particle.SendoHamonOverdriveParticle;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.TargetHitPart;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class CustomParticlesHelper {
    
    private static final Map<ResourceLocation, IAnimatedSprite> SPRITE_SETS = new HashMap<>();
    
    public static void saveSprites(Minecraft mc) {
        Map<ResourceLocation, ? extends IAnimatedSprite> spritesMap = ClientReflection.getSpriteSets(mc.particleEngine);
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.MENACING.get());
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
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_GREEN.get());
        CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_RAINBOW.get());
    }
    
    public static IAnimatedSprite getSavedSpriteSet(ParticleType<?> particleType) {
        return SPRITE_SETS.get(ForgeRegistries.PARTICLE_TYPES.getKey(particleType));
    }
    
    private static void saveSprites(Map<ResourceLocation, ? extends IAnimatedSprite> sprites, ParticleType<?> particleType) {
        ResourceLocation key = ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
        SPRITE_SETS.put(key, sprites.get(key));
    }
    
    
    
    public static void addMenacingParticleEmitter(Entity entity, BasicParticleType particle) {
        Minecraft mc = Minecraft.getInstance();
        ClientReflection.getTrackingEmitters(Minecraft.getInstance().particleEngine).add(
                new MenacingParticleEmitter(mc.level, entity, particle, mc.player));
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
    
    public static void summonHamonAuraParticlesFirstPerson(IParticleData type, LivingEntity user, float particlesPerTick) {
        IAnimatedSprite sprite = getSavedSpriteSet(type.getType());
        if (sprite != null) {
            Random random = user.getRandom();
            FirstPersonHamonAura particles = FirstPersonHamonAura.getInstance();
            
            for (HandSide handSide : HandSide.values()) {
                GeneralUtil.doFractionTimes(() -> {
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
    
    // FIXME use chariot's armor layer if it is on
    public static <T extends StandEntity> void addStandCrumbleParticles(T standEntity, Vector3d pos, TargetHitPart humanoidPart) {
        EntityRenderer<? super T> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(standEntity);
        if (renderer instanceof StandEntityRenderer) {
            StandEntityRenderer<? super T, ?> standRenderer = (StandEntityRenderer<? super T, ?>) renderer;
            StandEntityModel<? super T> model = standRenderer.getModel(standEntity);
            
            ResourceLocation texture = renderer.getTextureLocation(standEntity);
            if (texture == null) return;
            
            ModelRenderer.TexturedQuad polygon = HumanoidStandModel.getRandomQuad(model.getRandomCubeAt(humanoidPart));
            if (polygon != null) {
                ModelRenderer.PositionTextureVertex[] vertices = polygon.vertices;
                if (vertices.length > 0) {
                    float u0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).min().getAsDouble();
                    float v0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).min().getAsDouble();
                    float u1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).max().getAsDouble();
                    float v1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).max().getAsDouble();
                    
                    Minecraft mc = Minecraft.getInstance();
                    double x = pos.x;
                    double y = pos.y;
                    double z = pos.z;
                    StandCrumbleParticle particle = new StandCrumbleParticle(mc.level, x, y, z, 0, 0, 0);
                    particle.setTextureAndUv(texture, u0, v0, u1, v1);
                    mc.particleEngine.add(particle);
                }
            }
            
        }
    }
    
    public static void addBlockBreakParticles(BlockPos blockPos, BlockState blockState) {
        Minecraft.getInstance().particleEngine.destroy(blockPos, blockState);
    }
    
    public static void addBlockShardBreakParticles(Vector3d pos, BlockState blockState) {
        Minecraft mc = Minecraft.getInstance();
        ParticleManager particleManager = mc.particleEngine;
        ClientWorld world = mc.level;
        BlockPos blockPos = new BlockPos(pos);
        if (!blockState.isAir(world, blockPos)) {
            for (int i = 0; i < 4; i++) {
                double x = (Math.random() - 0.5) * 0.2;
                double y = (Math.random() - 0.5) * 0.2;
                double z = (Math.random() - 0.5) * 0.2;
                particleManager.add(new DiggingParticle(world, pos.x + x, pos.y + y, pos.z + z, 
                        x * 0.25, y * 0.25, z * 0.25, blockState).init(blockPos));
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
