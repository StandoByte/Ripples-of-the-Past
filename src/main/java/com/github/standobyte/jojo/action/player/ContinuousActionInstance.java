package com.github.standobyte.jojo.action.player;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

public abstract class ContinuousActionInstance<T extends ContinuousActionInstance<T, P>, P extends IPower<P, ?>> {
    protected final LivingEntity user;
    protected final PlayerUtilCap userCap;
    protected final P playerPower;
    protected final IPlayerAction<T, P> action;
    protected int tick = 0;
    private boolean stop = false;
    
    public ContinuousActionInstance(LivingEntity user, PlayerUtilCap userCap, P playerPower, IPlayerAction<T, P> action) {
        this.user = user;
        this.userCap = userCap;
        this.action = action;
        this.playerPower = playerPower;
    }
    
    public void tick() {
        action.playerTick(getThis());
        tick++;
        int maxTicks = getMaxDuration();
        if (maxTicks > 0 && tick >= maxTicks) {
            stopAction();
        }
    }
    
    protected abstract T getThis();
    
    public LivingEntity getUser() {
        return user;
    }
    
    public P getPower() {
        return playerPower;
    }
    
    public IPlayerAction<T, P> getAction() {
        return action;
    }
    
    public Action<?> getActionSync() {
        return action instanceof Action ? (Action<?>) action : null;
    }
    
    public int getTick() {
        return tick;
    }
    
    public boolean stopAction() {
        if (!stop) {
            stop = true;
            return true;
        }
        return false;
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
    
    protected boolean isMeleeAttack(DamageSource dmgSource) {
        return dmgSource.getEntity() != null && dmgSource.getDirectEntity() != null && dmgSource.getEntity().is(dmgSource.getDirectEntity());
    }
    
    
    public static Optional<ContinuousActionInstance<?, ?>> getCurrentAction(LivingEntity entity) {
        return entity.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve().flatMap(living -> living.getContinuousAction());
    }
}
