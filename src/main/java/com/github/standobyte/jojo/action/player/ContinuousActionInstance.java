package com.github.standobyte.jojo.action.player;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerContinuousActionPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

public abstract class ContinuousActionInstance<T extends IPlayerAction<?, P>, P extends IPower<P, ?>> {
    protected final LivingEntity user;
    protected final PlayerUtilCap userCap;
    protected final P playerPower;
    protected final T action;
    private Phase phase;
    protected int tick = 0;
    private boolean stop = false;
    protected int actionCooldown;
    
    public ContinuousActionInstance(LivingEntity user, PlayerUtilCap userCap, P playerPower, T action) {
        this.user = user;
        this.userCap = userCap;
        this.action = action;
        this.playerPower = playerPower;
        if (action instanceof Action) {
            actionCooldown = ((Action<P>) action).getCooldown(playerPower, -1);
        }
    }
    
    public void onStart() {}
    
    public final void tick() {
        if (phase == null) {
            phase = Phase.PERFORM;
        }
        playerTick();
        tick++;
        int maxTicks = getMaxDuration();
        if (maxTicks > 0 && tick >= maxTicks) {
            stopAction();
        }
    }
    
    protected void playerTick() {}
    
    public LivingEntity getUser() {
        return user;
    }
    
    public P getPower() {
        return playerPower;
    }
    
    public T getAction() {
        return action;
    }
    
    public Action<?> getActionSync() {
        return action instanceof Action ? (Action<?>) action : null;
    }
    
    public int getTick() {
        return tick;
    }
    
    public Phase getPhase() {
        return phase;
    }
    
    public void setPhase(Phase phase) {
        setPhase(phase, false);
    }
    
    public void setPhase(Phase phase, boolean sync) {
        if (this.phase != phase) {
            onPhaseSet(this.phase, phase);
        }
        this.phase = phase;
        this.tick = 0;
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(TrPlayerContinuousActionPacket.setPhase(user.getId(), phase), user);
        }
    }
    
    protected void onPhaseSet(@Nullable Phase oldPhase, Phase nextPhase) {}
    
    public final boolean stopAction() {
        if (!stop) {
            stop = true;
            onStop();
            return true;
        }
        return false;
    }
    
    public void onStop() {
        if (!user.level.isClientSide() && actionCooldown > 0) {
            playerPower.setCooldownTimer((Action<P>) action, actionCooldown);
        }
    }
    
    public boolean isStopped() {
        return stop;
    }
    
    public int getMaxDuration() {
        return -1;
    }
    
    public float getWalkSpeed() {
        return 1;
    }
    
    public boolean updateTarget() {
        return false;
    }
    
    public boolean cancelIncomingDamage(DamageSource dmgSource, float dmgAmount) {
        return false;
    }
    
    @Deprecated
    protected boolean isMeleeAttack(DamageSource dmgSource) {
        return DamageUtil.isMeleeAttack(dmgSource);
    }
    
    
    public static Optional<ContinuousActionInstance<?, ?>> getCurrentAction(LivingEntity entity) {
        return entity.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve().flatMap(living -> living.getContinuousAction());
    }
}
