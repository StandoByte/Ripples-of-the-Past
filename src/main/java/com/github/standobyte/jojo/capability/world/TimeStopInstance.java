package com.github.standobyte.jojo.capability.world;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.actions.TimeStop;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

// TODO implement IStandEffect
public class TimeStopInstance {
    private static int i = 0;
    private final World world;
    private final int id;
    private final TimeStop action;
    
    private int ticksLeft;
    private int ticksPassed = 0;
    final ChunkPos centerPos;
    final int chunkRange;
    @Nullable
    final LivingEntity user;
    private final LazyOptional<IStandPower> userPower;
    private final Optional<TimeStopperStandStats> statsOptional;
    private EffectInstance statusEffectInstance;
    
    @Nullable
    private SoundEvent timeResumeSound;
    @Nullable
    private SoundEvent timeResumeVoiceLine;
    @Nullable
    private SoundEvent timeManualResumeVoiceLine;
    private boolean ticksManuallySet = false;
    private boolean alwaysSayVoiceLine = false;
    
    public TimeStopInstance(World world, int ticks, ChunkPos pos, int chunkRange, LivingEntity user, TimeStop action) {
        this(world, ticks, pos, chunkRange, user, action, i++);
    }
    
    public TimeStopInstance(World world, int ticks, ChunkPos pos, int chunkRange, LivingEntity user, TimeStop action, int id) {
        this.world = world;
        this.ticksLeft = ticks;
        this.centerPos = pos;
        this.chunkRange = chunkRange;
        this.user = user;
        this.userPower = IStandPower.getStandPowerOptional(user);
        this.statsOptional = Optional.ofNullable(userPower.map(power -> power.hasPower() && power.getType().getStats() instanceof TimeStopperStandStats
                ? (TimeStopperStandStats) power.getType().getStats() : null).orElse(null));
        this.action = action;
        this.id = id;
    }
    
    public void setSounds(SoundEvent timeResumeSound, SoundEvent timeResumeVoiceLine, SoundEvent timeManualResumeVoiceLine) {
        this.timeResumeSound = timeResumeSound;
        this.timeResumeVoiceLine = timeResumeVoiceLine;
        this.timeManualResumeVoiceLine = timeManualResumeVoiceLine;
    }
    
    public void setStatusEffectInstance(EffectInstance effectInstance) {
        this.statusEffectInstance = effectInstance;
    }
    
    public boolean tick() {
        ticksPassed++;
        if (user != null) {
            if (!user.isAlive()) {
                return true;
            }
            
            if (userPower.map(power -> {
                if (power.hasPower()) {
                    float staminaCost = power.getStaminaTickGain();
                    if (action != null) {
                        staminaCost += action.getStaminaCostTicking(power);
                    }
                    if (!power.consumeStamina(staminaCost) && !StandUtil.standIgnoresStaminaDebuff(user)) {
                        power.setStamina(0);
                        return true;
                    }
                    return false;
                }
                else {
                    return true;
                }
            }).orElse(false)) {
                return true;
            }
            
            tickSounds();
        }
        return --ticksLeft <= 0;
    }
    
    public void setTicksLeft(int ticks) {
        if (!ticksManuallySet && ticksLeft > TIME_RESUME_VOICELINE_TICKS && ticks < TIME_RESUME_VOICELINE_TICKS) {
            alwaysSayVoiceLine = true;
        }
        this.ticksLeft = ticks;
        ticksManuallySet = true;
    }
    
    public boolean wereTicksManuallySet() {
        return ticksManuallySet;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }
    
    public boolean isTimeStopped(ChunkPos pos) {
        return ticksLeft > 0 && inRange(pos);
    }
    
    public boolean inRange(ChunkPos pos) {
        if (chunkRange <= 0) {
            return true;
        }
        return Math.abs(centerPos.x - pos.x) < chunkRange && Math.abs(centerPos.z - pos.z) < chunkRange;
    }
    
    public static final int TIME_RESUME_SOUND_TICKS = 10;
    public static final int TIME_RESUME_VOICELINE_TICKS = 30;
    public void tickSounds() {
        if (!world.isClientSide() && (ticksLeft == TIME_RESUME_SOUND_TICKS || ticksLeft == TIME_RESUME_VOICELINE_TICKS || alwaysSayVoiceLine)) {
            if (ticksLeft == TIME_RESUME_SOUND_TICKS) {
                if (timeResumeSound != null) {
                    PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeResumeSound, SoundCategory.AMBIENT, user.blockPosition(), 5.0F, 1.0F), 
                            world.dimension(), player -> inRange(TimeStopHandler.getChunkPos(player)) && TimeUtil.canPlayerSeeInStoppedTime(player));
                }
            }
            else {
                SoundEvent voiceLine = ticksManuallySet ? timeManualResumeVoiceLine : timeResumeVoiceLine;
                if (voiceLine != null) {
                    JojoModUtil.sayVoiceLine(user, voiceLine);
                }
                alwaysSayVoiceLine = false;
            }
        }
    }
    
    public void removeSoundsIfCrosses(TimeStopInstance newInstance) {
        if (this.ticksLeft < newInstance.ticksLeft && newInstance.inRange(this.centerPos)) {
            timeResumeSound = null;
            timeResumeVoiceLine = null;
            timeManualResumeVoiceLine = null;
        }
    }
    
    public void onRemoved(World world) {
        if (!world.isClientSide()) {
            if (action != null) {
                userPower.ifPresent(power -> {
                    statsOptional.ifPresent(stats -> {
                        float cooldown;
                        if (power.isUserCreative()) {
                            cooldown = 0;
                        }
                        else {
                            cooldown = stats.cooldownPerTick * ticksPassed;
                            if (power.getUser() != null && power.getUser().hasEffect(ModEffects.RESOLVE.get())) {
                                cooldown /= 3F;
                            }
                        }
                        power.setCooldownTimer(action, (int) cooldown);

                        power.addLearningProgressPoints(action, stats.maxDurationGrowthPerTick * ticksPassed);
                    });
                });
            }
            if (user != null && statusEffectInstance != null) {
                JojoModUtil.removeEffectInstance(user, statusEffectInstance);
            }
        }
    }
    
    public SoundEvent getTimeResumeSound() {
        return timeResumeSound;
    }
    
    public int getId() {
        return id;
    }
    
    public void syncToClient(ServerPlayerEntity player) {
        SyncWorldTimeStopPacket packet;
        if (ticksLeft > 0) {
            boolean canMove = TimeUtil.canPlayerMoveInStoppedTime(player, true);
            boolean canSee = TimeUtil.canPlayerSeeInStoppedTime(canMove, TimeUtil.hasTimeStopAbility(player));
            packet = new SyncWorldTimeStopPacket(ticksLeft, id, centerPos, 
                    canSee, canMove, user == null ? -1 : user.getId(), action);
        }
        else {
            packet = SyncWorldTimeStopPacket.timeResumed(id);
        }
        PacketManager.sendToClient(packet, player);
    }
}
