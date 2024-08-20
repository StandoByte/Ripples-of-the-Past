package com.github.standobyte.jojo.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ICloudRenderHandler;
import net.minecraftforge.client.ISkyRenderHandler;
import net.minecraftforge.client.IWeatherParticleRenderHandler;
import net.minecraftforge.client.IWeatherRenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TemporaryDimensionEffects {
    private static TemporaryDimensionEffects instance = new TemporaryDimensionEffects();
    
    public static void init() {
        MinecraftForge.EVENT_BUS.register(instance);
    }
    
    public static TemporaryDimensionEffects getInstance() {
        return instance;
    }
    
    
    private Map<ResourceLocation, DimensionEffectsStack> effectsByDimension = new HashMap<>();
    
    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (effectsByDimension.isEmpty() || event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() == null) {
            return;
        }
        
        ResourceLocation key = mc.level.dimension().location();
        DimensionEffectsStack stack = effectsByDimension.get(key);
        if (stack == null) {
            return;
        }
        
        if (stack.tick(mc.level)) {
            effectsByDimension.remove(key);
        }
    }
    
    public DimensionEffectsStack getEffectsStack(ClientWorld world) {
        DimensionEffectsStack stack = effectsByDimension.computeIfAbsent(
                world.dimension().location(), __ -> new DimensionEffectsStack(world));
        return stack;
    }
    
    
    
    public static class DimensionEffectsStack {
        private DimensionEffect prevOtherEffects;
        private Stack<DimensionEffect> temporaryEffectsStack = new Stack<>();
        
        private DimensionEffectsStack(ClientWorld world) {
            this.prevOtherEffects = new DimensionEffect();
            this.prevOtherEffects.saveEffects(world.effects());
        }
        
        public boolean addEffect(ClientWorld world, DimensionEffect effect) {
            if (temporaryEffectsStack.isEmpty() || temporaryEffectsStack.peek() != effect) {
                temporaryEffectsStack.add(effect);
                effect.isActive = true;
                effect.setTo(world.effects());
                return true;
            }
            return false;
        }
        
        public boolean addEffectLast(ClientWorld world, DimensionEffect effect) {
            if (temporaryEffectsStack.isEmpty() || temporaryEffectsStack.firstElement() != effect) {
                temporaryEffectsStack.insertElementAt(effect, 0);
                effect.isActive = true;
                effect.setTo(world.effects());
                return true;
            }
            return false;
        }
        
        private boolean tick(ClientWorld world) {
            Iterator<DimensionEffect> iter = temporaryEffectsStack.iterator();
            boolean updateEffects = false;
            while (iter.hasNext()) {
                DimensionEffect effects = iter.next();
                if (effects.isActive()) {
                    break;
                }
                else {
                    iter.remove();
                    updateEffects = true;
                }
            }
            
            if (updateEffects) {
                DimensionEffect effects = temporaryEffectsStack.isEmpty() ? prevOtherEffects : temporaryEffectsStack.peek();
                effects.setTo(world.effects());
            }
            
            return temporaryEffectsStack.isEmpty();
        }
    }
    
    
    
    public static class DimensionEffect {
        private IWeatherRenderHandler weatherRenderer;
        private IWeatherParticleRenderHandler weatherParticleRenderer;
        private ISkyRenderHandler skyRenderer;
        private ICloudRenderHandler cloudRenderer;
        
        private boolean isActive;
        
        public DimensionEffect withWeatherRenderer(IWeatherRenderHandler weatherRenderer) {
            this.weatherRenderer = weatherRenderer;
            return this;
        }
        
        public DimensionEffect withWeatherParticleRenderer(IWeatherParticleRenderHandler weatherParticleRenderer) {
            this.weatherParticleRenderer = weatherParticleRenderer;
            return this;
        }
        
        public DimensionEffect withSkyRenderer(ISkyRenderHandler skyRenderer) {
            this.skyRenderer = skyRenderer;
            return this;
        }
        
        public DimensionEffect withCloudRenderer(ICloudRenderHandler cloudRenderer) {
            this.cloudRenderer = cloudRenderer;
            return this;
        }
        
        public void saveEffects(DimensionRenderInfo worldEffects) {
            this
            .withWeatherRenderer(worldEffects.getWeatherRenderHandler())
            .withWeatherParticleRenderer(worldEffects.getWeatherParticleRenderHandler())
            .withSkyRenderer(worldEffects.getSkyRenderHandler())
            .withCloudRenderer(worldEffects.getCloudRenderHandler());
        }
        
        public void setTo(DimensionRenderInfo dimensionEffects) {
            dimensionEffects.setWeatherRenderHandler(weatherRenderer);
            dimensionEffects.setWeatherParticleRenderHandler(weatherParticleRenderer);
            dimensionEffects.setSkyRenderHandler(skyRenderer);
            dimensionEffects.setCloudRenderHandler(cloudRenderer);
        }
        
        public void setActive(boolean active) {
            this.isActive = active;
        }
        
        public boolean isActive() {
            return isActive;
        }
        
        public DimensionEffect copy() {
            DimensionEffect effects = new DimensionEffect();
            effects.weatherRenderer = this.weatherRenderer;
            effects.weatherParticleRenderer = this.weatherParticleRenderer;
            effects.skyRenderer = this.skyRenderer;
            effects.cloudRenderer = this.cloudRenderer;
            return effects;
        }
    }
    
}
