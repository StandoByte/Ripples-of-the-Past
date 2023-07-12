package com.github.standobyte.jojo.action.stand;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class MagiciansRedRedBind extends StandEntityAction {
    public static final StandPose RED_BIND_POSE = new StandPose("MR_RED_BIND");

    public MagiciansRedRedBind(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            standEntity.addProjectile(new MRRedBindEntity(world, standEntity));
            standEntity.playSound(ModSounds.MAGICIANS_RED_RED_BIND.get(), 1.0F, 1.0F);
        }
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld, boolean willFire) {
        invokeForStand(power, stand -> {
            if (stand.getCurrentTaskAction() == this) {
                if (stand.willHeavyPunchBeFinisher() && getLandedRedBind(stand).isPresent()) {
                    return;
                }
                else {
                    stand.stopTaskWithRecovery();
                }
            }
        });
    }
    
    public static Optional<MRRedBindEntity> getLandedRedBind(StandEntity stand) {
        List<MRRedBindEntity> redBindLanded = stand.level.getEntitiesOfClass(MRRedBindEntity.class, 
                stand.getBoundingBox().inflate(16), redBind -> stand.is(redBind.getOwner()) && redBind.isAttachedToAnEntity());
        return !redBindLanded.isEmpty() ? Optional.of(redBindLanded.get(0)) : Optional.empty();
    }
    
    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (power.isActive() && ((StandEntity) power.getStandManifestation()).willHeavyPunchBeFinisher()) {
            return null;
        }
        return super.getShout(user, power, target, wasActive);
    }
    
    @Override
    public boolean noFinisherDecay() {
        return true;
    }
}
