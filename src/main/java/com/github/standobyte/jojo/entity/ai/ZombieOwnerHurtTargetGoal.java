package com.github.standobyte.jojo.entity.ai;

import java.util.EnumSet;

import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;

public class ZombieOwnerHurtTargetGoal extends TargetGoal {
    private final HungryZombieEntity zombie;
    private LivingEntity attacker;
    private int timestamp;

    public ZombieOwnerHurtTargetGoal(HungryZombieEntity zombie) {
        super(zombie, false);
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = zombie.getOwner();
        if (!zombie.isOwnerNearby() || owner == null) {
            return false;
        } else {
            this.attacker = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            return i != timestamp && canAttack(attacker, EntityPredicate.DEFAULT) && zombie.wantsToAttack(attacker, owner);
        }
    }


    @Override
    public void start() {
        mob.setTarget(attacker);
        LivingEntity owner = zombie.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}