package com.github.standobyte.jojo.client.render.world;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

// that's so hacky...
public class ParticleManagerWrapperTS extends ParticleManager {
    private static ParticleManagerWrapperTS instance;
    
    public static void onTimeStopStart(Minecraft mc) {
        ClientReflection.setParticleEngine(mc, init(mc));
    }
    
    public static void onTimeStopEnd(Minecraft mc) {
        if (instance != null) {
            ClientReflection.setParticleEngine(mc, instance.actualParticleEngine);
        }
    }
    
    private static ParticleManagerWrapperTS init(Minecraft mc) {
        if (instance == null) {
            instance = new ParticleManagerWrapperTS(mc);
        }
        return instance;
    }
    
    
    
    private final ParticleManager actualParticleEngine;

    private ParticleManagerWrapperTS(Minecraft mc) {
        super(mc.level, new DummyTextureManager(mc.getResourceManager()));
        this.actualParticleEngine = mc.particleEngine;
        ClientReflection.setTextureManager(this, mc.textureManager);
    }
    
    // too late to register any way
    @Override
    public <T extends IParticleData> void register(ParticleType<T> pParticleType, IParticleFactory<T> pParticleFactory) {}

    @Override
    public <T extends IParticleData> void register(ParticleType<T> pParticleType, ParticleManager.IParticleMetaFactory<T> pParticleMetaFactory) {}

    @Override
    public CompletableFuture<Void> reload(IFutureReloadListener.IStage pStage, IResourceManager pResourceManager, IProfiler pPreparationsProfiler, IProfiler pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
        return actualParticleEngine.reload(pStage, pResourceManager, pPreparationsProfiler, pReloadProfiler, pBackgroundExecutor, pGameExecutor);
    }

    @Override
    public void close() {
        actualParticleEngine.close();
    }

    // FIXME add certain particles during time stop
    @Override
    public void createTrackingEmitter(Entity pEntity, IParticleData pParticleData) {
        
    }

    // FIXME add certain particles during time stop
    @Override
    public void createTrackingEmitter(Entity pEntity, IParticleData pData, int pLifetime) {
        
    }

    // FIXME add certain particles during time stop
    @Override
    @Nullable
    public Particle createParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        return null;
    }

    // FIXME add certain particles during time stop
    @Override
    public void add(Particle pEffect) {
        
    }

    @Override
    public void tick() {
        
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks) {
        actualParticleEngine.render(pMatrixStack, pBuffer, pLightTexture, pActiveRenderInfo, 1.0F);
    }

    @Override
    public void renderParticles(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, @Nullable ClippingHelper clippingHelper) {
        actualParticleEngine.renderParticles(pMatrixStack, pBuffer, pLightTexture, pActiveRenderInfo, 1.0F, clippingHelper);
    }

    @Override
    public void setLevel(@Nullable ClientWorld pLevel) {
        actualParticleEngine.setLevel(pLevel);
    }

    @Override
    public void destroy(BlockPos pPos, BlockState pState) {
        actualParticleEngine.destroy(pPos, pState);
    }

    @Override
    public void crack(BlockPos pPos, Direction pSide) {
        actualParticleEngine.crack(pPos, pSide);
    }

    @Override
    public String countParticles() {
       return actualParticleEngine.countParticles();
    }

    @Override
    public void addBlockHitEffects(BlockPos pos, BlockRayTraceResult target) {
        actualParticleEngine.addBlockHitEffects(pos, target);
    }
    
    
    
    private static class DummyTextureManager extends TextureManager {

        private DummyTextureManager(IResourceManager pResourceManager) {
            super(pResourceManager);
        }

        @Override
        public void register(ResourceLocation pTextureLocation, Texture pTextureObj) {}
    }
}
