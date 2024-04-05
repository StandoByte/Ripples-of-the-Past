package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

public class VampirismClawLacerate extends VampirismAction {

    public VampirismClawLacerate(VampirismAction.Builder builder) {
        super(builder.doNotCancelClick());
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
    	switch (target.getType()) {
    	case BLOCK:
    		return ActionConditionResult.POSITIVE;
    	case ENTITY:
    		return ActionConditionResult.POSITIVE;
    	default:
    		return ActionConditionResult.NEGATIVE;
    	}
    }
 
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
    	switch (target.getType()) {
    	case BLOCK:
            BlockPos pos = target.getBlockPos();
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
//                            dropItem = false;
                        }
                    }
                    if (digDuration >= 0 && digDuration <= 2.5F * Math.sqrt(user.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                        world.destroyBlock(pos, dropItem);
                        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), user.getSoundSource(), 1.2F, 0.8F);
                    }
                    else {
                        SoundType soundType = blockState.getSoundType(world, pos, user);
                        world.playSound(null, pos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
                    }
                }
            }
            break;
        case ENTITY:
        	if (!world.isClientSide() && target.getType() == TargetType.ENTITY) {
                Entity entity = target.getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity targetEntity = (LivingEntity) entity;
                    PlayerEntity pEntity = (PlayerEntity) user;
                    if (entity.hurt(EntityDamageSource.playerAttack(pEntity), getDamage(world))) {
                    	world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), ModSounds.THE_WORLD_PUNCH_HEAVY_ENTITY.get(), targetEntity.getSoundSource(), 1.2F, 0.8F);
                    	targetEntity.knockback(2F, user.getX()-targetEntity.getX(), user.getZ()-targetEntity.getZ());
                    }
                }
            }
        	break;
        default:
            break;
    }
}
    
    protected float getDamage(World world) {
        return 8.0F + world.getDifficulty().getId();
    }

}
