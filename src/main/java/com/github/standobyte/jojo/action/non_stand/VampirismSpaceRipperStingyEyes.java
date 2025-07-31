package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SpaceRipperStingyEyesEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class VampirismSpaceRipperStingyEyes extends VampirismAction implements IPlayerAction<VampirismSpaceRipperStingyEyes.Instance, INonStandPower> {

    public VampirismSpaceRipperStingyEyes(NonStandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected int maxCuringStage() {
        return 1;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!user.level.isClientSide()) {
            setPlayerAction(user, power);
        }
    }
    
    @Override
    public Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        return new Instance(user, userCap, power, this);
    }
    
    
    
    public static class Instance extends ContinuousActionInstance<VampirismSpaceRipperStingyEyes, INonStandPower> {
        private SpaceRipperStingyEyesEntity[] lasers = new SpaceRipperStingyEyesEntity[2];

        public Instance(LivingEntity user, PlayerUtilCap userCap, INonStandPower playerPower,
                VampirismSpaceRipperStingyEyes action) {
            super(user, userCap, playerPower, action);
        }
        
        @Override
        public void onStart() {
            World world = user.level;
            if (!world.isClientSide()) {
                world.addFreshEntity(lasers[0] = new SpaceRipperStingyEyesEntity(world, user, true));
                world.addFreshEntity(lasers[1] = new SpaceRipperStingyEyesEntity(world, user, false));
            }
        }
        
        static final int TICK_DURATION = 20;
        @Override
        public void playerTick() {
            if (!user.level.isClientSide()) {
                float tickEnergyCost = action.getEnergyCost(playerPower, ActionTarget.EMPTY);
                boolean hasEnergy = playerPower.consumeEnergy(tickEnergyCost);
                if (!hasEnergy) {
                    playerPower.setEnergy(0);
                }
                if (tick >= TICK_DURATION || !hasEnergy) {
                    for (SpaceRipperStingyEyesEntity laser : lasers) {
                        if (laser != null) laser.detach();
                    }
                    playerPower.setCooldownTimer(getAction(), (int) (50f * (float) tick / TICK_DURATION));
                    stopAction();
                }
            }
        }
        
        @Override
        public float getWalkSpeed() {
            return 0.3f;
        }
        
    }

}
