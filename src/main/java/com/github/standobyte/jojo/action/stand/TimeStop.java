package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.config.ActionConfigField;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
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

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

public class TimeStop extends StandAction {
    @ActionConfigField private final int timeStopMaxTicks;
    @ActionConfigField private final int timeStopMaxTicksVampire;
    @ActionConfigField private final int timeStopMaxTicksPillarman;
    @ActionConfigField public final float timeStopLearningPerTick;
    @ActionConfigField public final float timeStopDecayPerDay;
    @ActionConfigField public final float timeStopCooldownPerTick;
    
    private final Supplier<SoundEvent> voiceLineWithStandSummoned;
    private final Supplier<SoundEvent> timeStopSound;
    private final Supplier<SoundEvent> timeResumeVoiceLine;
    private final Supplier<SoundEvent> timeManualResumeVoiceLine;
    private final Supplier<SoundEvent> timeResumeSound;
    private final ResourceLocation shaderWithAnim;
    private final ResourceLocation shaderOld;

    public TimeStop(TimeStop.Builder builder) {
        super(builder);
        this.timeStopMaxTicks = builder.timeStopMaxTicks;
        this.timeStopMaxTicksVampire = builder.timeStopMaxTicksVampire;
        this.timeStopMaxTicksPillarman = builder.timeStopMaxTicksPillarman;
        this.timeStopLearningPerTick = builder.timeStopLearningPerTick;
        this.timeStopDecayPerDay = builder.timeStopDecayPerDay;
        this.timeStopCooldownPerTick = builder.timeStopCooldownPerTick;
        
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
            
            // TODO event
            timeStopTicks = instance.getTicksLeft();
            
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
    
    @Override
    public void passivelyOnNewDay(LivingEntity user, IStandPower power, long prevDay, long day) {
        if (!user.level.isClientSide()) {
            LivingUtilCap cap = user.getCapability(LivingUtilCapProvider.CAPABILITY).resolve().get();
            if (!cap.hasUsedTimeStopToday && timeStopDecayPerDay > 0) {
                power.addLearningProgressPoints(this, -timeStopDecayPerDay);
            }
            cap.hasUsedTimeStopToday = false;
        }
    }
    
    
    
    public static final int MIN_TIME_STOP_TICKS = 5;
    public static int getTimeStopTicks(IStandPower standPower, StandAction timeStopAction) {
        return MathHelper.floor(standPower.getLearningProgressPoints(timeStopAction)) + MIN_TIME_STOP_TICKS;
    }
    
    public int getMaxTimeStopTicks(IStandPower standPower) {
        LivingEntity livingEntity = standPower.getUser();
        return TimeStop.pillarmanTimeStopDuration(livingEntity) ? timeStopMaxTicksPillarman : TimeStop.vampireTimeStopDuration(livingEntity) ? timeStopMaxTicksVampire : timeStopMaxTicks;
    }
    
    public static boolean vampireTimeStopDuration(LivingEntity entity) {
        return ModPowers.VAMPIRISM.get().isHighOnBlood(entity);
    }

    public static boolean pillarmanTimeStopDuration(LivingEntity entity) {
        return ModPowers.PILLAR_MAN.get().isHighLifeForce(entity);
    }
    
    
    
    public static class Builder extends StandAction.AbstractBuilder<Builder> {
        private int timeStopMaxTicks = 100;
        private int timeStopMaxTicksVampire = 180;
        private int timeStopMaxTicksPillarman = 180;
        private float timeStopLearningPerTick = 0.1F;
        private float timeStopDecayPerDay = 0;
        private float timeStopCooldownPerTick = 3;
        
        private Supplier<SoundEvent> voiceLineWithStandSummoned = () -> null;
        private Supplier<SoundEvent> timeStopSound = () -> null;
        private Supplier<SoundEvent> timeResumeVoiceLine = () -> null;
        private Supplier<SoundEvent> timeManualResumeVoiceLine = () -> null;
        private Supplier<SoundEvent> timeResumeSound = () -> null;
        private ResourceLocation shaderWithAnim = new ResourceLocation(JojoMod.MOD_ID, "shaders/post/time_stop_tw.json");
        private ResourceLocation shaderOld = new ResourceLocation(JojoMod.MOD_ID, "shaders/post/time_stop_tw_old.json");

        public Builder timeStopMaxTicks(int forHuman, int forVampire, int forPillarman) {
            forHuman = Math.max(TimeStop.MIN_TIME_STOP_TICKS, forHuman);
            forVampire = Math.max(forHuman, forVampire);
            forPillarman = Math.max(forHuman, forPillarman);
            this.timeStopMaxTicks = forHuman;
            this.timeStopMaxTicksVampire = forVampire;
            this.timeStopMaxTicksPillarman = forVampire;
            return getThis();
        }

        public Builder timeStopMaxTicks(int forHuman, int forVampire) {
            return timeStopMaxTicks(forHuman, forVampire, forVampire);
        }
        
        public Builder timeStopLearningPerTick(float points) {
            this.timeStopLearningPerTick = points;
            return getThis();
        }
        
        public Builder timeStopDecayPerDay(float points) {
            this.timeStopDecayPerDay = points;
            return getThis();
        }
        
        public Builder timeStopCooldownPerTick(float ticks) {
            this.timeStopCooldownPerTick = ticks;
            return getThis();
        }
        
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
