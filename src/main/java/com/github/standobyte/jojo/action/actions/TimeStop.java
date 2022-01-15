package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

//FIXME for SP: auto-summon stand
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
        if (wasActive && voiceLineWithStandSummoned != null) {
            return voiceLineWithStandSummoned.get();
        }
        return super.getShout(user, power, target, wasActive);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        if (user.hasEffect(ModEffects.TIME_STOP.get())) {
            return ActionConditionResult.NEGATIVE;
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        int timeStopTicks = TimeHandler.getTimeStopTicks(getXpRequirement(), power, user, INonStandPower.getNonStandPowerOptional(user));
        if (!world.isClientSide()) {
            power.setXp(power.getXp() + 4);
            BlockPos blockPos = user.blockPosition();
            ChunkPos chunkPos = new ChunkPos(blockPos);
            TimeHandler.setTimeResumeSounds(world, chunkPos, timeStopTicks, this, user);
            TimeHandler.stopTime(world, timeStopTicks, chunkPos);
            if (timeStopTicks >= 40 && timeStopSound != null && timeStopSound.get() != null) {
                PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeStopSound.get(), SoundCategory.AMBIENT, blockPos, 5.0F, 1.0F), 
                        world.dimension(), player -> (JojoModConfig.COMMON.inTimeStopRange(
                                chunkPos, new ChunkPos(player.blockPosition()))) && TimeHandler.canPlayerSeeInStoppedTime(player));
            }
            user.addEffect(new EffectInstance(ModEffects.TIME_STOP.get(), timeStopTicks, 0, false, false, true));
        }
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
    public TranslationTextComponent getTranslatedName(IStandPower power, String key) {
        LivingEntity user = power.getUser();
        int timeStopTicks = TimeHandler.getTimeStopTicks(getXpRequirement(), power, user, INonStandPower.getNonStandPowerOptional(user));
        return new TranslationTextComponent(key, String.format("%.2f", (float) timeStopTicks / 20F));
    }
}
