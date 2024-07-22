package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

public class PillarmanBladeBarrage extends PillarmanAction {

    public PillarmanBladeBarrage(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.LIGHT;
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!(MCUtil.isHandFree(user, Hand.MAIN_HAND) && MCUtil.isHandFree(user, Hand.OFF_HAND))) {
            return conditionMessage("hands");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
        	Entity targetEntity = target.getEntity();
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
                            digDuration /= player.getDigSpeed(blockState, pos);
                            if (player.abilities.instabuild) {
                                digDuration = 0;
                                dropItem = false;
                            }
                            else if (!ForgeHooks.canHarvestBlock(blockState, player, world, pos)) {
                                digDuration *= 10F / 3F;
//                                dropItem = false;
                            }
                        }
                        if (digDuration >= 0 && digDuration <= 2.5F * Math.sqrt(user.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                            world.destroyBlock(pos, dropItem);
                        }
                        else {
                            SoundType soundType = blockState.getSoundType(world, pos, user);
                            world.playSound(null, pos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
                        }
                    }
                }
                break;
            case ENTITY:
            	if(targetEntity instanceof LivingEntity) {
            		LivingEntity targetLiving = (LivingEntity) targetEntity;
	                if (user instanceof PlayerEntity) {
	                    int invulTicks = targetEntity.invulnerableTime;
	                    targetEntity.invulnerableTime = invulTicks;
	                }
	                if (!world.isClientSide()) {
	                    DamageUtil.hurtThroughInvulTicks(targetLiving, EntityDamageSource.playerAttack((PlayerEntity) user), 
	                            (DamageUtil.getDamageWithoutHeldItem(user)*0.2F));
	                }
            	}
                break;
            default:
                break;
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.SILVER_CHARIOT_BARRAGE_SWIPE.get(), user.getSoundSource(), 1.0F, 1.0F);
            PillarmanDivineSandstorm.auraEffect(user, ParticleTypes.SWEEP_ATTACK, 1);
        }
    }
    
    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (requirementsFulfilled) {
            if (ticksHeld % 2 == 0) {
                user.swinging = false;
                user.swing(ticksHeld % 4 == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
            }
        }
    }
}
