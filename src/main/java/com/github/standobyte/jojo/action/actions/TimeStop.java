package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public class TimeStop extends StandAction {
    private Supplier<SoundEvent> voiceLineWithStandSummoned = () -> null;
    private Supplier<SoundEvent> timeStopSound = () -> null;
    private Supplier<SoundEvent> timeResumeVoiceLine = () -> null;
    private Supplier<SoundEvent> timeResumeSound = () -> null;

    public TimeStop(StandAction.Builder builder) {
        super(builder);
    }

    public TimeStop voiceLineWithStandSummoned(Supplier<SoundEvent> voiceLine) {
        this.voiceLineWithStandSummoned = voiceLine;
        return this;
    }

    public TimeStop timeStopSound(Supplier<SoundEvent> sound) {
        this.timeStopSound = sound;
        return this;
    }

    public TimeStop timeResumeVoiceLine(Supplier<SoundEvent> voiceLine) {
        this.timeResumeVoiceLine = voiceLine;
        return this;
    }

    public TimeStop timeResumeSound(Supplier<SoundEvent> sound) {
        this.timeResumeSound = sound;
        return this;
    }

    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (TimeHandler.isTimeStopped(user.level, user.blockPosition())) {
            return null;
        }
        if (wasActive && voiceLineWithStandSummoned != null && voiceLineWithStandSummoned.get() != null) {
            return voiceLineWithStandSummoned.get();
        }
        return super.getShout(user, power, target, wasActive);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user.hasEffect(ModEffects.TIME_STOP.get())) {
            return ActionConditionResult.NEGATIVE;
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        int timeStopTicks = getTimeStopTicks(power, this, user, INonStandPower.getNonStandPowerOptional(user));
        if (!world.isClientSide()) {
            BlockPos blockPos = user.blockPosition();
            ChunkPos chunkPos = new ChunkPos(blockPos);
            boolean invadingStoppedTime = TimeHandler.isTimeStopped(world, user.blockPosition());
            TimeHandler.setTimeResumeSounds(world, chunkPos, timeStopTicks, this, user);
            TimeHandler.stopTime(world, timeStopTicks, chunkPos);
            if (timeStopTicks >= 40 && timeStopSound != null && timeStopSound.get() != null
                    && !invadingStoppedTime) {
                PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeStopSound.get(), SoundCategory.AMBIENT, blockPos, 5.0F, 1.0F), 
                        world.dimension(), player -> (JojoModConfig.getCommonConfigInstance(false).inTimeStopRange(
                                chunkPos, new ChunkPos(player.blockPosition()))) && TimeHandler.canPlayerSeeInStoppedTime(player));
            }
            // FIXME (!!!!) add progress points inside TickingTimeStopInstance instead
            power.addLearningProgressPoints(this, 5);
            if (hasShiftVariation()) {
                power.addLearningProgressPoints(getShiftVariationIfPresent(), 5);
            }
            user.addEffect(new EffectInstance(ModEffects.TIME_STOP.get(), timeStopTicks, 0, false, false, true));
        }
    }
    
    @Override
    public float getStaminaCost(IStandPower stand) {
        return super.getStaminaCost(stand) + getStaminaCostTicking(stand) * 100;
    }
    
    @Override
    public int getCooldownTechnical(IStandPower power) {
        return getTimeStopTicks(power, this, power.getUser(), INonStandPower.getNonStandPowerOptional(power.getUser()));
    }

    @Override
    public int getHoldDurationToFire(IStandPower power) { 
        return TimeHandler.isTimeStopped(power.getUser().level, power.getUser().blockPosition()) ? 0 : super.getHoldDurationToFire(power);
    }

    @Nullable
    public SoundEvent getTimeResumeSfx() {
        return timeResumeSound.get();
    }

    @Nullable
    public SoundEvent getTimeResumeVoiceLine() {
        return timeResumeVoiceLine.get();
    }

    @Override
    public float getMaxTrainingPoints(IStandPower power) {
        return getMaxTimeStopTicks(power) - MIN_TIME_STOP_TICKS;
    }

    @Override
    public TranslationTextComponent getTranslatedName(IStandPower power, String key) {
        LivingEntity user = power.getUser();
        int timeStopTicks = getTimeStopTicks(power, this, user, INonStandPower.getNonStandPowerOptional(user));
        return new TranslationTextComponent(key, String.format("%.2f", (float) timeStopTicks / 20F));
    }
    
    @Override
    public boolean canUserSeeInStoppedTime(LivingEntity user, IStandPower power) {
        return true;
    }

    
    
    public static final int MIN_TIME_STOP_TICKS = 5;
    public static int getTimeStopTicks(IStandPower standPower, StandAction timeStopAction, LivingEntity user, LazyOptional<INonStandPower> otherPower) {
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
