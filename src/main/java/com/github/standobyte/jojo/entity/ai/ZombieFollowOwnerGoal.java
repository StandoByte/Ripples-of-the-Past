package com.github.standobyte.jojo.entity.ai;

import java.util.EnumSet;

import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;

public class ZombieFollowOwnerGoal extends Goal {
    private final HungryZombieEntity zombie;
    private LivingEntity owner;
    private final double followSpeed;
    private final PathNavigator navigator;
    private int timeToRecalcPath;
    private final float maxDist;
    private final float minDist;
    private float oldWaterCost;

    public ZombieFollowOwnerGoal(HungryZombieEntity zombie, double followSpeed, float minDist, float maxDist, boolean canFly) {
        this.zombie = zombie;
        this.followSpeed = followSpeed;
        this.navigator = zombie.getNavigation();
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(zombie.getNavigation() instanceof GroundPathNavigator) && !(zombie.getNavigation() instanceof FlyingPathNavigator)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = zombie.getOwner();
        if (owner == null) {
            return false;
        } else if (owner.isSpectator()) {
            return false;
        } else if (zombie.distanceToSqr(owner) < (double) (minDist * minDist)) {
            return false;
        } else {
            this.owner = owner;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (navigator.isDone()) {
            return false;
        } else {
            return !(zombie.distanceToSqr(owner) <= (double) (maxDist * maxDist));
        }
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = zombie.getPathfindingMalus(PathNodeType.WATER);
        zombie.setPathfindingMalus(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        owner = null;
        navigator.stop();
        zombie.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        zombie.getLookControl().setLookAt(owner, 10.0F, (float) zombie.getMaxHeadXRot());
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = 10;
            if (!zombie.isLeashed() && !zombie.isPassenger() && !zombie.farFromOwner(12)) {
                navigator.moveTo(owner, followSpeed);
            }
        }
    }
}