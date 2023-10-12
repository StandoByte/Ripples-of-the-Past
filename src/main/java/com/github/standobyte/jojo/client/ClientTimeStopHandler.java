package com.github.standobyte.jojo.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.ClientTicking.ITicking;
import com.github.standobyte.jojo.client.render.world.ParticleManagerWrapperTS;
import com.github.standobyte.jojo.client.render.world.TimeStopWeatherHandler;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.client.IWeatherParticleRenderHandler;
import net.minecraftforge.client.IWeatherRenderHandler;

public class ClientTimeStopHandler implements ITicking {
    private static ClientTimeStopHandler instance;
    
    private final Minecraft mc;
    private boolean isTimeStopped = false;
    private boolean canSeeInStoppedTime = true;
    private boolean canMoveInStoppedTime = true;
    private float partialTickStoppedAt;

    private int timeStopTicks = 0;
    private int timeStopLength = 0;
    
    
    private ClientTimeStopHandler(Minecraft mc) {
        this.mc = mc;
    }
    
    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ClientTimeStopHandler(mc);
            ClientTicking.addTicking(instance);
        }
    }
    
    public static ClientTimeStopHandler getInstance() {
        return instance;
    }
    
    
    private boolean isTimeStopped(BlockPos blockPos) {
        return isTimeStopped(new ChunkPos(blockPos));
    }

    private boolean isTimeStopped(ChunkPos chunkPos) {
        return mc.level != null && TimeStopHandler.isTimeStopped(mc.level, chunkPos);
    }

    public void setTimeStopClientState(boolean canSee, boolean canMove) {
        canSeeInStoppedTime = canSee;
        setCanMoveInStoppedTime(canSee && canMove);
        partialTickStoppedAt = canMove ? mc.getFrameTime() : 0.0F;
        ShaderEffectApplier.getInstance().setResetShader();
    }

    
    public void updateCanMoveInStoppedTime(boolean canMove, ChunkPos chunkPos) {
        if (isTimeStopped(chunkPos)) {
            setCanMoveInStoppedTime(canMove);
        }
    }
    
    private void setCanMoveInStoppedTime(boolean canMove) {
        this.canMoveInStoppedTime = canMove;
        mc.player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            if (!canMove) {
                cap.lockXRot();
                cap.lockYRot();
            }
            else {
                cap.clearLockedXRot();
                cap.clearLockedYRot();
            }
        });
    }

    
    public void updateTimeStopTicksLeft() {
        if (mc.level != null && mc.player != null) {
            int ticks = TimeStopHandler.getTimeStopTicksLeft(mc.level, new ChunkPos(mc.player.blockPosition()));
            this.timeStopLength = timeStopTicks + ticks;
        }
        else {
            this.timeStopLength = 0;
        }
    }
    
    private final Map<ClientWorld, Pair<IWeatherRenderHandler, IWeatherParticleRenderHandler>> prevWeatherRender = new HashMap<>();
    private final TimeStopWeatherHandler timeStopWeatherHandler = new TimeStopWeatherHandler();
    private Set<ITickable> prevTickableTextures = new HashSet<>();
    private void setTimeStoppedState(boolean isTimeStopped) {
        if (this.isTimeStopped != isTimeStopped) {
            this.isTimeStopped = isTimeStopped;
            
            if (!isTimeStopped) {
                timeStopLength = 0;
            }
            
            if (JojoModConfig.CLIENT.timeStopFreezesVisuals.get()) {
                if (isTimeStopped) {
                    if (mc.level != null) {
                        DimensionRenderInfo effects = mc.level.effects();
                        prevWeatherRender.put(mc.level, Pair.of(effects.getWeatherRenderHandler(), effects.getWeatherParticleRenderHandler()));
                        effects.setWeatherRenderHandler(timeStopWeatherHandler);
                        effects.setWeatherParticleRenderHandler(timeStopWeatherHandler);
    
                        TextureManager textureManager = mc.getTextureManager();
                        prevTickableTextures = ClientReflection.getTickableTextures(textureManager);
                        ClientReflection.setTickableTextures(textureManager, new HashSet<>());
                        
                        ParticleManagerWrapperTS.onTimeStopStart(mc);
                    }
                }
                else {
                    if (mc.level != null && prevWeatherRender.containsKey(mc.level)) {
                        timeStopWeatherHandler.onTimeStopEnd();
                        Pair<IWeatherRenderHandler, IWeatherParticleRenderHandler> prevEffects = prevWeatherRender.get(mc.level);
                        DimensionRenderInfo effects = mc.level.effects();
                        effects.setWeatherRenderHandler(prevEffects.getLeft());
                        effects.setWeatherParticleRenderHandler(prevEffects.getRight());
                    }
    
                    TextureManager textureManager = mc.getTextureManager();
                    Set<ITickable> allTickableTextures = Util.make(new HashSet<>(), set -> {
                        set.addAll(prevTickableTextures);
                        set.addAll(ClientReflection.getTickableTextures(textureManager));
                    });
                    ClientReflection.setTickableTextures(textureManager, allTickableTextures);
                    prevTickableTextures = new HashSet<>();
    
                    ParticleManagerWrapperTS.onTimeStopEnd(mc);
                }
            }
            timeStopTicks = 0;
        }
    }
    
    
    @Override
    public void tick() {
        if (isTimeStopped) {
            timeStopTicks++;
        }
    }
    
    public void tickPauseIrrelevant() {
        if (mc.level != null) {
            setTimeStoppedState(isTimeStopped(mc.player.blockPosition()));
            if (isTimeStopped && !canSeeInStoppedTime) {
                ClientReflection.pauseClient(mc);
            }
        }
        else if (isTimeStopped) {
            setTimeStoppedState(false);
        }
    }
    
    
    public void setConstantPartialTick(Timer clientTimer) {
        if (isTimeStopped() && !canSeeInStoppedTime) {
            clientTimer.partialTick = partialTickStoppedAt;
        }
    }
    
    public float getConstantEntityPartialTick(Entity entity, float normalPartialTick) {
        if (!entity.canUpdate() && isTimeStopped(entity.blockPosition())) {
            return partialTickStoppedAt;
        }
        return normalPartialTick;
    }
    
    public boolean shouldCancelSound(ISound sound) {
        return !canSeeInStoppedTime && sound != null && sound.getAttenuation() == AttenuationType.LINEAR;
    }
    
    
    public boolean isTimeStopped() {
        return isTimeStopped;
    }

    public boolean canSeeInStoppedTime() {
        return canSeeInStoppedTime;
    }
    
    public int getTimeStopTicks() {
        return timeStopTicks;
    }
    
    public int getTimeStopLength() {
        return timeStopLength;
    }
}
