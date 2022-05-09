package com.github.standobyte.jojo.action.actions;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.TimeUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TimeStop extends StandAction {
    private final float learningPerTick;
    private Supplier<SoundEvent> voiceLineWithStandSummoned = () -> null;
    private Supplier<SoundEvent> timeStopSound = () -> null;
    private Supplier<SoundEvent> timeResumeVoiceLine = () -> null;
    private Supplier<SoundEvent> timeManualResumeVoiceLine = () -> null;
    private Supplier<SoundEvent> timeResumeSound = () -> null;

    public TimeStop(StandAction.Builder builder, float learningPerTick) {
        super(builder);
        this.learningPerTick = learningPerTick;
    }

    public TimeStop voiceLineWithStandSummoned(Supplier<SoundEvent> voiceLine) {
        this.voiceLineWithStandSummoned = voiceLine;
        return this;
    }

    public TimeStop timeStopSound(Supplier<SoundEvent> sound) {
        this.timeStopSound = sound;
        return this;
    }

    public TimeStop addTimeResumeVoiceLine(Supplier<SoundEvent> voiceLine) {
        timeManualResumeVoiceLine = voiceLine;
        timeResumeVoiceLine = voiceLine;
        return this;
    }

    public TimeStop addTimeResumeVoiceLine(Supplier<SoundEvent> voiceLine, boolean useOnManualResume) {
        if (useOnManualResume) {
            timeManualResumeVoiceLine = voiceLine;
        }
        else {
            timeResumeVoiceLine = voiceLine;
        }
        return this;
    }

    public TimeStop timeResumeSound(Supplier<SoundEvent> sound) {
        this.timeResumeSound = sound;
        return this;
    }

    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (TimeUtil.isTimeStopped(user.level, user.blockPosition())) {
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
            boolean invadingStoppedTime = TimeUtil.isTimeStopped(world, user.blockPosition());
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
            
            EffectInstance immunityEffect = new EffectInstance(ModEffects.TIME_STOP.get(), timeStopTicks, 0, false, false, true);
            user.addEffect(immunityEffect);
            instance.setStatusEffectInstance(immunityEffect);
            
            TimeUtil.stopTime(world, instance);
            if (timeStopTicks >= 40 && timeStopSound != null && timeStopSound.get() != null
                    && !invadingStoppedTime) {
                PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeStopSound.get(), SoundCategory.AMBIENT, blockPos, 5.0F, 1.0F), 
                        world.dimension(), player -> instance.inRange(TimeStopHandler.getChunkPos(player)) && TimeUtil.canPlayerSeeInStoppedTime(player));
            }
        }
    }

    @Override
    public int getHoldDurationToFire(IStandPower power) { 
        return TimeUtil.isTimeStopped(power.getUser().level, power.getUser().blockPosition()) ? 0 : super.getHoldDurationToFire(power);
    }

    @Override
    public float getMaxTrainingPoints(IStandPower power) {
        return getMaxTimeStopTicks(power) - MIN_TIME_STOP_TICKS;
    }
    
    @Override
    public boolean canUserSeeInStoppedTime(LivingEntity user, IStandPower power) {
        return true;
    }
    
    public float getLearningPerTick() {
        return learningPerTick;
    }
    
    @Override
    public void onTrainingPoints(IStandPower power, float points) {
        if (hasShiftVariation()) {
            power.setLearningProgressPoints(super.getShiftVariationIfPresent(), points, false, false);
        }
    }
    
    @Override
    public TranslationTextComponent getTranslatedName(IStandPower power, String key) {
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
    
    public int getCooldownAfterTimeStop(IStandPower power, int ticksTimeStop) {
        return super.getCooldownAdditional(power, -1) * ticksTimeStop;
    }
    
    
    
    public static final int MIN_TIME_STOP_TICKS = 5;
    public static int getTimeStopTicks(IStandPower standPower, StandAction timeStopAction) {
        return MathHelper.floor(standPower.getLearningProgressPoints(timeStopAction)) + MIN_TIME_STOP_TICKS;
    }
    
    public static int getMaxTimeStopTicks(IStandPower standPower) {
        return ((TimeStopperStandStats) standPower.getType().getStats())
                .getMaxTimeStopTicks(TimeStop.vampireTimeStopDuration(standPower.getUser()));
    }
    
    public static boolean vampireTimeStopDuration(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity).map(power -> {
            if (power.getType() == ModNonStandPowers.VAMPIRISM.get()) {
                return power.getEnergy() / power.getMaxEnergy() >= 0.8F;
            }
            return false;
        }).orElse(false);
    }
}
