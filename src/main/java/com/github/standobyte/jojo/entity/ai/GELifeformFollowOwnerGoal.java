package com.github.standobyte.jojo.entity.ai;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.world.server.ServerWorld;

public class GELifeformFollowOwnerGoal extends Goal {
    protected final MobEntity mob;
    private final double speedModifier;
    protected final UUID ownerUuid;
    protected Entity owner;

    public GELifeformFollowOwnerGoal(MobEntity lifeform, UUID ownerUuid, double speedModifier) {
        this.mob = lifeform;
        this.speedModifier = speedModifier;
        this.ownerUuid = ownerUuid;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse() {
        if (owner != null && !owner.isAlive()) {
            owner = null;
        }
        if (owner == null && ownerUuid != null && !mob.level.isClientSide()) {
            owner = ((ServerWorld) mob.level).getEntity(ownerUuid);
        }
        return this.owner != null && this.owner.isAlive();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void tick() {
//        this.mob.getLookControl().setLookAt(this.owner, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
//        if (this.mob.distanceToSqr(this.owner) < 6.25D) {
//            this.mob.getNavigation().stop();
//        } else {
            this.mob.getNavigation().moveTo(this.owner, this.speedModifier);
//        }
    }
}
