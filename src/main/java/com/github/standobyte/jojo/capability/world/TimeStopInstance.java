package com.github.standobyte.jojo.capability.world;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.action.stand.TimeStopInstant;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

// TODO implement IStandEffect
public class TimeStopInstance {
    private static int i = 0;
    private final World world;
    private final int id;
    private final TimeStop action;
    private final int startingTicks;
    
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
        this.startingTicks = ticks;
        this.ticksLeft = ticks;
        this.centerPos = pos;
        this.chunkRange = chunkRange;
        this.user = user;
        this.userPower = IStandPower.getStandPowerOptional(user);
        this.statsOptional = Optional.ofNullable(userPower.map(power -> {
            if (!power.hasPower()) return null;
            StandStats stats = power.getType().getStats();
            return stats instanceof TimeStopperStandStats ? (TimeStopperStandStats) stats : null;
        }).orElse(null));
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
        ticksLeft--;
        if (world.isClientSide() && user != ClientUtil.getClientPlayer()) {
            return false;
        }
        
        if (user != null) {
            if (!user.isAlive()) {
                return true;
            }
            
            if (userPower.map(power -> {
                if (power.hasPower()) {
                    if (!(power.getType().getStats() instanceof TimeStopperStandStats)) {
                        return true;
                    }
                    float staminaCost = power.getStaminaTickGain();
                    if (action != null) {
                        staminaCost += action.getStaminaCostTicking(power);
                    }
                    if (!power.consumeStamina(staminaCost, true)) {
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
        return ticksLeft <= 0;
    }
    
    public void setTicksLeft(int ticks) {
        if (!ticksManuallySet && ticksLeft > TIME_RESUME_VOICELINE_TICKS && ticks < TIME_RESUME_VOICELINE_TICKS) {
            alwaysSayVoiceLine = true;
        }
        this.ticksLeft = ticks;
        ticksManuallySet = true;
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            
            serverWorld.players().forEach(player -> {
                if (player.level == world) {
                    PacketManager.sendToClient(TimeStopInstancePacket.setTicks(id, ticksLeft), player);
                }
            });
        }
    }
    
    public boolean wereTicksManuallySet() {
        return ticksManuallySet;
    }
    
    public int getStartingTicks() {
        return startingTicks;
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
        if (!world.isClientSide()) {
            if (ticksLeft == TIME_RESUME_SOUND_TICKS) {
                if (timeResumeSound != null) {
                    PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeResumeSound, SoundCategory.AMBIENT, user.blockPosition(), 5.0F, 1.0F), 
                            world.dimension(), player -> inRange(TimeStopHandler.getChunkPos(player)) && TimeStopHandler.canPlayerSeeInStoppedTime(player));
                }
            }
            else if (ticksLeft == TIME_RESUME_VOICELINE_TICKS || alwaysSayVoiceLine) {
                if (startingTicks >= 100) {
                    SoundEvent voiceLine = ticksManuallySet ? timeManualResumeVoiceLine : timeResumeVoiceLine;
                    if (voiceLine != null) {
                        JojoModUtil.sayVoiceLine(user, voiceLine);
                    }
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
                    if (power.hasPower()) {
                        statsOptional.ifPresent(stats -> {
                            float cooldown = getTimeStopCooldown(power, stats, ticksPassed);
                            power.setCooldownTimer(action, (int) cooldown);
                            if (action.getInstantTSVariation() != null) {
                                power.setCooldownTimer(action.getInstantTSVariation(), (int) (cooldown * TimeStopInstant.COOLDOWN_RATIO));
                            }

                            if (power.getType().getStats() instanceof TimeStopperStandStats) {
                                power.addLearningProgressPoints(action, stats.timeStopLearningPerTick * ticksPassed);
                            }
                        });
                    }
                });
            }
            if (user != null && statusEffectInstance != null) {
                MCUtil.removeEffectInstance(user, statusEffectInstance);
            }
        }
    }
    
    public static float getTimeStopCooldown(IStandPower power, TimeStopperStandStats stats, int ticks) {
        float cooldown;
        if (power.isUserCreative()) {
            cooldown = 0;
        }
        else {
            cooldown = stats.timeStopCooldownPerTick * ticks;
            if (power.getUser() != null && power.getUser().hasEffect(ModStatusEffects.RESOLVE.get())) {
                cooldown /= 3F;
            }
        }
        return cooldown;
    }
    
    public SoundEvent getTimeResumeSound() {
        return timeResumeSound;
    }
    
    public int getId() {
        return id;
    }
    
    public void syncToClient(ServerPlayerEntity player) {
        TimeStopInstancePacket packet;
        if (ticksLeft > 0) {
            packet = new TimeStopInstancePacket(ticksLeft, id, centerPos, user == null ? -1 : user.getId(), action);
        }
        else {
            packet = TimeStopInstancePacket.timeResumed(id);
        }
        PacketManager.sendToClient(packet, player);
    }
}
