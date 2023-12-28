package com.github.standobyte.jojo.action.stand;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.stats.TimeStopperStandStats;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TimeStop extends StandAction {
    private final Supplier<SoundEvent> voiceLineWithStandSummoned;
    private final Supplier<SoundEvent> timeStopSound;
    private final Supplier<SoundEvent> timeResumeVoiceLine;
    private final Supplier<SoundEvent> timeManualResumeVoiceLine;
    private final Supplier<SoundEvent> timeResumeSound;
    private final ResourceLocation shaderWithAnim;
    private final ResourceLocation shaderOld;

    public TimeStop(TimeStop.Builder builder) {
        super(builder);
        this.voiceLineWithStandSummoned = builder.voiceLineWithStandSummoned;
        this.timeStopSound = builder.timeStopSound;
        this.timeResumeVoiceLine = builder.timeResumeVoiceLine;
        this.timeManualResumeVoiceLine = builder.timeManualResumeVoiceLine;
        this.timeResumeSound = builder.timeResumeSound;
        this.shaderWithAnim = builder.shaderWithAnim;
        this.shaderOld = builder.shaderOld;
    }

    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (TimeStopHandler.isTimeStopped(user.level, user.blockPosition())) {
            return null;
        }
        if (wasActive && voiceLineWithStandSummoned != null && voiceLineWithStandSummoned.get() != null) {
            return voiceLineWithStandSummoned.get();
        }
        return super.getShout(user, power, target, wasActive);
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int timeStopTicks = getTimeStopTicks(power, this);
            BlockPos blockPos = user.blockPosition();
            ChunkPos chunkPos = new ChunkPos(blockPos);
            boolean invadingStoppedTime = TimeStopHandler.isTimeStopped(world, user.blockPosition());
            TimeStopInstance instance = new TimeStopInstance(world, timeStopTicks, chunkPos, 
                    JojoModConfig.getCommonConfigInstance(world.isClientSide()).timeStopChunkRange.get(), user, this);
            Optional<TimeStopInstance> currentMaxInstance = world.getCapability(WorldUtilCapProvider.CAPABILITY)
                    .map(cap -> cap.getTimeStopHandler().getInstancesInPos(chunkPos).stream().max(Comparator.comparingInt(TimeStopInstance::getTicksLeft)))
                    .orElse(Optional.empty());
            
            if (invadingStoppedTime && currentMaxInstance.map(TimeStopInstance::getTicksLeft).orElse(0) > timeStopTicks) {
                instance.setSounds(
                        currentMaxInstance.get().getTimeResumeSound(), 
                        null, 
                        null);
            }
            else {
                instance.setSounds(
                        timeResumeSound.get(), 
                        timeResumeVoiceLine.get(), 
                        timeManualResumeVoiceLine.get());
            }
            
            EffectInstance immunityEffect = new EffectInstance(ModStatusEffects.TIME_STOP.get(), timeStopTicks, 0, false, false, true);
            user.addEffect(immunityEffect);
            instance.setStatusEffectInstance(immunityEffect);
            
            TimeStopHandler.stopTime(world, instance);
            if (timeStopTicks >= 40 && timeStopSound != null && timeStopSound.get() != null
                    && !invadingStoppedTime) {
                PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeStopSound.get(), SoundCategory.AMBIENT, blockPos, 5.0F, 1.0F), 
                        world.dimension(), player -> instance.inRange(TimeStopHandler.getChunkPos(player)) && TimeStopHandler.canPlayerSeeInStoppedTime(player));
            }
            
            user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.hasUsedTimeStopToday = true);
        }
    }

    @Override
    public int getHoldDurationToFire(IStandPower power) { 
        return TimeStopHandler.isTimeStopped(power.getUser().level, power.getUser().blockPosition()) ? 0 : super.getHoldDurationToFire(power);
    }

    @Override
    public float getMaxTrainingPoints(IStandPower power) {
        return getMaxTimeStopTicks(power) - MIN_TIME_STOP_TICKS;
    }
    
    @Override
    public boolean canUserSeeInStoppedTime(LivingEntity user, IStandPower power) {
        return true;
    }
    
    @Override
    public void onTrainingPoints(IStandPower power, float points) {
        if (getInstantTSVariation() != null) {
            power.setLearningProgressPoints(getInstantTSVariation(), points);
        }
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        int timeStopTicks = getTimeStopTicks(power, this);
        return new TranslationTextComponent(key, String.format("%.2f", (float) timeStopTicks / 20F));
    }
    
    @Override
    public float getStaminaCostTicking(IStandPower power) {
        return super.getStaminaCostTicking(power) * 100 / getTimeStopTicks(power, this);
    }
    
    @Override
    public int getCooldownTechnical(IStandPower power) {
        return getTimeStopTicks(power, this);
    }
    
    @Override
    protected int getCooldownAdditional(IStandPower power, int ticksHeld) {
        return 0;
    }
    
    @Nullable
    public StandAction getInstantTSVariation() {
        return blink;
    }
    
    private StandAction blink;
    void setInstantTSVariation(StandAction blink) {
        this.blink = blink;
    }
    
    @Nullable
    public ResourceLocation getTimeStopShader(boolean withAnimEffect) {
        return withAnimEffect ? shaderWithAnim : shaderOld;
    }
    
    
    
    public static final int MIN_TIME_STOP_TICKS = 5;
    public static int getTimeStopTicks(IStandPower standPower, StandAction timeStopAction) {
        return MathHelper.floor(standPower.getLearningProgressPoints(timeStopAction)) + MIN_TIME_STOP_TICKS;
    }
    
    public static int getMaxTimeStopTicks(IStandPower standPower) {
        StandStats stats = standPower.getType().getStats();
        if (stats instanceof TimeStopperStandStats) {
            return ((TimeStopperStandStats) stats).getMaxTimeStopTicks(
                    TimeStop.vampireTimeStopDuration(standPower.getUser()));
        }
        return 100;
    }
    
    public static boolean vampireTimeStopDuration(LivingEntity entity) {
        return ModPowers.VAMPIRISM.get().isHighOnBlood(entity);
    }
    
    
    
    public static class Builder extends StandAction.AbstractBuilder<Builder> {
        private Supplier<SoundEvent> voiceLineWithStandSummoned = () -> null;
        private Supplier<SoundEvent> timeStopSound = () -> null;
        private Supplier<SoundEvent> timeResumeVoiceLine = () -> null;
        private Supplier<SoundEvent> timeManualResumeVoiceLine = () -> null;
        private Supplier<SoundEvent> timeResumeSound = () -> null;
        private ResourceLocation shaderWithAnim = new ResourceLocation(JojoMod.MOD_ID, "shaders/post/time_stop_tw.json");
        private ResourceLocation shaderOld = new ResourceLocation(JojoMod.MOD_ID, "shaders/post/time_stop_tw_old.json");
        
        public Builder voiceLineWithStandSummoned(Supplier<SoundEvent> voiceLine) {
            this.voiceLineWithStandSummoned = voiceLine;
            return getThis();
        }
        
        public Builder timeStopSound(Supplier<SoundEvent> sound) {
            this.timeStopSound = sound;
            return getThis();
        }
        
        public Builder addTimeResumeVoiceLine(Supplier<SoundEvent> voiceLine) {
            timeManualResumeVoiceLine = voiceLine;
            timeResumeVoiceLine = voiceLine;
            return getThis();
        }
        
        public Builder addTimeResumeVoiceLine(Supplier<SoundEvent> voiceLine, boolean useOnManualResume) {
            if (useOnManualResume) {
                timeManualResumeVoiceLine = voiceLine;
            }
            else {
                timeResumeVoiceLine = voiceLine;
            }
            return getThis();
        }
        
        public Builder timeResumeSound(Supplier<SoundEvent> sound) {
            this.timeResumeSound = sound;
            return getThis();
        }
        
        public Builder shaderEffect(ResourceLocation path, boolean withAnimEffect) {
            if (withAnimEffect) {
                this.shaderWithAnim = path;
            }
            else {
                this.shaderOld = path;
            }
            return getThis();
        }
        
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
