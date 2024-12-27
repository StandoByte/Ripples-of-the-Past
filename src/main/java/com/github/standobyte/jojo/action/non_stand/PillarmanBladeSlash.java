package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class PillarmanBladeSlash extends PillarmanAction implements IPlayerAction<PillarmanBladeSlash.Instance, INonStandPower> {

    public PillarmanBladeSlash(PillarmanAction.Builder builder) {
        super(builder);
        mode = Mode.LIGHT;
    }
    
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            setPlayerAction(user, power);
        }
    }
    
    @Override
    public PillarmanBladeSlash.Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.bladeSlash.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    @Override
    public void setCooldownOnUse(INonStandPower power) {}   
    
    public static class Instance extends ContinuousActionInstance<PillarmanBladeSlash, INonStandPower> {
        
        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, PillarmanBladeSlash action) {
            super(user, userCap, playerPower, action);
        }
        
        @Override
        public void playerTick() {
            switch (getTick()) {
            case 2:
            	playerPower.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().setBladesVisible(true);
            	break;
            case 6:
                if (user.level.isClientSide()) {
                    user.level.playSound(ClientUtil.getClientPlayer(), user.getX(), user.getEyeY(), user.getZ(), 
                            ModSounds.HAMON_SYO_SWING.get(), user.getSoundSource(), 1.0f, 1.25f); // TODO separate sound event
                    user.swing(Hand.MAIN_HAND, true);
                }
                break;
            case 8:
                if (!user.level.isClientSide()) {
                    ActionTarget target = playerPower.getMouseTarget();
                    punchPerform(user.level, user, playerPower, target, ModSounds.THE_WORLD_PUNCH_HEAVY_ENTITY.get(), 1.2F, 0.8F); // TODO separate sound event
                }
                break;
            case 20:
                stopAction();
                break;
            }
        }
        
        public void punchPerform(World world, LivingEntity user, INonStandPower power, ActionTarget target, SoundEvent sound, float volume, float pitch) {
        	if (!world.isClientSide()) {
                Entity entity = target.getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity targetEntity = (LivingEntity) entity;
                    PlayerEntity pEntity = (PlayerEntity) user;
                    if (entity.hurt(EntityDamageSource.playerAttack(pEntity), DamageUtil.addArmorPiercing(VampirismClawLacerate.getDamage(world, user) + 1F, 15F, targetEntity))) {
                    	PillarmanUtil.sparkEffect(targetEntity, 9);
                    	world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), sound, targetEntity.getSoundSource(), volume, pitch);
                        targetEntity.knockback(0.75F, user.getX() - targetEntity.getX(), user.getZ() - targetEntity.getZ());
                        KnockbackCollisionImpact.getHandler(targetEntity).ifPresent(cap -> cap
                                .onPunchSetKnockbackImpact(targetEntity.getDeltaMovement(), user)
                                );
                    }
                }
            }
        }
        
        @Override
        public boolean updateTarget() {
            return true;
        }
        
        
        @Override
        public float getWalkSpeed() {
            return 0.5f;
        }
        
        @Override
        public void onStop() {
            super.onStop();
            if (user.level.isClientSide() && user instanceof PlayerEntity) {
            	playerPower.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().setBladesVisible(false);
                ModPlayerAnimations.bladeSlash.setAnimEnabled((PlayerEntity) user, false);
            }
        }
        
    }

}
