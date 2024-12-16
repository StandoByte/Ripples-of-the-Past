package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

public class VampirismClawLacerate extends VampirismAction implements IPlayerAction<VampirismClawLacerate.Instance, INonStandPower> {

    public VampirismClawLacerate(VampirismAction.Builder builder) {
        super(builder);
    }
    
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            setPlayerAction(user, power);
        }
    }
    
    @Override
    public VampirismClawLacerate.Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.vampireClawSwipe.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    @Override
    public void setCooldownOnUse(INonStandPower power) {}
    
    @Override
    protected void consumeEnergy(World world, LivingEntity user, INonStandPower power, ActionTarget target) {}
    
    
    public static class Instance extends ContinuousActionInstance<VampirismClawLacerate, INonStandPower> {
        
        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, VampirismClawLacerate action) {
            super(user, userCap, playerPower, action);
        }
        
        @Override
        public void playerTick() {
            switch (getTick()) {
            case 3:
                if (user.level.isClientSide()) {
                    user.level.playSound(ClientUtil.getClientPlayer(), user.getX(), user.getEyeY(), user.getZ(), 
                            ModSounds.VAMPIRE_SWIPE.get(), user.getSoundSource(), 1.0f, 1.25f);
                    user.swing(Hand.MAIN_HAND, true);
                }
                break;
            case 5:
                if (!user.level.isClientSide()) {
                    ActionTarget target = playerPower.getMouseTarget();
                    punchPerform(user.level, user, playerPower, target, ModSounds.VAMPIRE_CLAW_LACERATE.get(), 1.2F, 0.8F);
                }
                break;
            case 8:
                stopAction();
                break;
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
                ModPlayerAnimations.vampireClawSwipe.setAnimEnabled((PlayerEntity) user, false);
            }
        }
        
    }
    
    
    
    public static void punchPerform(World world, LivingEntity user, INonStandPower power, ActionTarget target, SoundEvent sound, float volume, float pitch) {
    	switch (target.getType()) {
        case BLOCK:
            if (JojoModUtil.breakingBlocksEnabled(world)) {
                blockDestroy(world, user, power, target, 0, 0, 0);
            	if ((power.getType() == ModPowers.PILLAR_MAN.get() && power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().getEvolutionStage() > 1)
            			|| power.getType() == ModPowers.VAMPIRISM.get()) {
            		blockDestroy(world, user, power, target, 1, 0, 0);
                	blockDestroy(world, user, power, target, -1, 0, 0);
                	blockDestroy(world, user, power, target, 0, 1, 0);
                	blockDestroy(world, user, power, target, 0, -1, 0);
                	blockDestroy(world, user, power, target, 0, 0, 1);
                	blockDestroy(world, user, power, target, 0, 0, -1);
                    
//                    Vector3d pos = Vector3d.atCenterOf(target.getBlockPos()).add(Vector3d.atLowerCornerOf(target.getFace().getNormal()).scale(0.6));
//                    HeavyPunchExplosion explosion = new HeavyPunchExplosion(world, user, target, 
//                            user.getLookAngle(), (user instanceof PlayerEntity ? DamageSource.playerAttack((PlayerEntity) user) : DamageSource.mobAttack(user)).setExplosion(), null, 
//                            pos.x, pos.y, pos.z, 
//                            1.0f /* stone is way too blast-resistant compared to dirt so it's commented out for now */, false, 
//                            JojoModUtil.breakingBlocksEnabled(user.level) ? Explosion.Mode.BREAK : Explosion.Mode.NONE);
//                    CustomExplosion.explode(explosion);
            	}
            }
        	world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.HEAVY_PUNCH.get(), user.getSoundSource(), 1.5F, 1.2F);
            break;
        case ENTITY:
            if (!world.isClientSide() && target.getType() == TargetType.ENTITY) {
                Entity entity = target.getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity targetEntity = (LivingEntity) entity;
                    PlayerEntity pEntity = (PlayerEntity) user;
                    if (entity.hurt(EntityDamageSource.playerAttack(pEntity), getDamage(world, user))) {
                        world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), sound, targetEntity.getSoundSource(), volume, pitch);
                        targetEntity.knockback(2F, user.getX() - targetEntity.getX(), user.getZ() - targetEntity.getZ());
                        
                        KnockbackCollisionImpact.getHandler(targetEntity).ifPresent(cap -> cap
                                .onPunchSetKnockbackImpact(targetEntity.getDeltaMovement(), user)
//                                .withImpactExplosion(1.0f, null, 0)
                                );
                    }
                }
            }
            break;
        default:
            break;
        }
    }
    
    public static void blockDestroy(World world, LivingEntity user, INonStandPower power, ActionTarget target, double x, double y, double z) {
    	BlockPos pos = target.getBlockPos().offset(x, y, z);
        if (!world.isClientSide() && JojoModUtil.canEntityDestroy((ServerWorld) world, pos, world.getBlockState(pos), user)) {
            if (!world.isEmptyBlock(pos)) {
                BlockState blockState = world.getBlockState(pos);
                float digDuration = blockState.getDestroySpeed(world, pos);
                boolean dropItem = true;
                if (user instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) user;
                    digDuration /= player.getDigSpeed(blockState, pos)/2;
                    if (player.abilities.instabuild) {
                        digDuration = 0;
                        dropItem = false;
                    }
                    else if (!ForgeHooks.canHarvestBlock(blockState, player, world, pos)) {
                        digDuration *= 1F / 3F;
//                        dropItem = false;
                    }
                }
                if (digDuration >= 0 && digDuration <= 2.5F * Math.sqrt(user.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                    MCUtil.destroyBlock(world, pos, dropItem, user);
                }
                else {
                    SoundType soundType = blockState.getSoundType(world, pos, user);
                    world.playSound(null, pos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
                }
            }
        }
    }
    
    public static float getDamage(World world, LivingEntity entity) {
        return (float) entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue() + 4;
    }
}
