package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanVeinEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class PillarmanErraticBlazeKing extends PillarmanAction implements IPlayerAction<PillarmanErraticBlazeKing.Instance, INonStandPower> {

    public PillarmanErraticBlazeKing(PillarmanAction.Builder builder) {
        super(builder);
        mode = Mode.HEAT;
    }
    
    
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            setPlayerAction(user, power);
        }
    }
    
    @Override
    public PillarmanErraticBlazeKing.Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.erraticBlazeKing.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    @Override
    public void setCooldownOnUse(INonStandPower power) {} // cooldown is set inside the continuous action instance
      
    public static class Instance extends ContinuousActionInstance<PillarmanErraticBlazeKing, INonStandPower> {
        
        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, PillarmanErraticBlazeKing action) {
            super(user, userCap, playerPower, action);
            
        }
        
        @Override
        public void playerTick() {
            switch (getTick()) {
            case 10:
                if (!user.level.isClientSide()) {
                	/*user.level.playSound(ClientUtil.getClientPlayer(), user.getX(), user.getEyeY(), user.getZ(), 
                            ModSounds.HAMON_SYO_SWING.get(), user.getSoundSource(), 1.0f, 1.5f);
                    user.swing(Hand.MAIN_HAND, true);*/
                	int n = 5;
                    for (int i = 0; i < n; i++) {
                        Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 10);
                        //addVeinProjectile(world, power, user, rotOffsets.x, rotOffsets.y, rotOffsets.x, rotOffsets.y - 0.6D);
                        addVeinProjectile(user.level, playerPower, user, rotOffsets.x, rotOffsets.y, -0.4, -0.45, 1);
                        addVeinProjectile(user.level, playerPower, user, rotOffsets.x, rotOffsets.y, 0.425, -0.575, 1);
                    }
                }
                break;
            case 40:
                stopAction();
                break;
            }
        }
        
        @Override
        public float getWalkSpeed() {
            return getAction().getHeldWalkSpeed();
        }
        
        @Override
        public void onStop() {
            super.onStop();
            if (user.level.isClientSide() && user instanceof PlayerEntity) {
                ModPlayerAnimations.erraticBlazeKing.setAnimEnabled((PlayerEntity) user, false);
            }
        }
        
        @Override
        public void onPreRender(float partialTick) {
            user.yBodyRot = user.yRot;
            user.yBodyRotO = user.yRotO;
        }
        
    }

    public static void addVeinProjectile(World world, INonStandPower power, LivingEntity user, float xRotDelta, float yRotDelta, double offsetX, double offsetY, double offsetZ) {
        PillarmanVeinEntity string = new PillarmanVeinEntity(world, user, xRotDelta, yRotDelta, offsetX, offsetY, offsetZ);
        string.setLifeSpan(25);
        world.addFreshEntity(string);
    }
    
}
