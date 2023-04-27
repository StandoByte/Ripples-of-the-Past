package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.StrayEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class VampirismFreeze extends VampirismAction {

    public VampirismFreeze(NonStandAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        if (user.isOnFire()) {
            return conditionMessage("fire");
        }
        if (user.level.dimensionType().ultraWarm()) {
            return conditionMessage("ultrawarm");
        }
        if (!user.getMainHandItem().isEmpty()) {
            return conditionMessage("hand");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            if (target.getType() == TargetType.ENTITY) {
                Entity entityTarget = target.getEntity();
                if (entityTarget instanceof LivingEntity && !entityTarget.isOnFire()) {
                    int difficulty = world.getDifficulty().getId();
                    LivingEntity targetLiving = (LivingEntity) entityTarget;
                    float damage = (float) Math.pow(2, difficulty);
                    if (targetLiving.getType() == EntityType.SKELETON && targetLiving.isAlive() && targetLiving.getHealth() <= damage) {
                        turnSkeletonIntoStray(targetLiving);
                    }
                    else if (DamageUtil.dealColdDamage(targetLiving, damage, user, null)) {
                        EffectInstance freezeInstance = targetLiving.getEffect(ModEffects.FREEZE.get());
                        if (freezeInstance == null) {
                            world.playSound(null, targetLiving, ModSounds.VAMPIRE_FREEZE.get(), targetLiving.getSoundSource(), 1.0F, 1.0F);
                            targetLiving.addEffect(new EffectInstance(ModEffects.FREEZE.get(), (difficulty + 1) * 50, 0));
                        }
                        else {
                            int additionalDuration = (difficulty - 1) * 5 + 1;
                            int duration = freezeInstance.getDuration() + additionalDuration;
                            int lvl = duration / 100;
                            targetLiving.addEffect(new EffectInstance(ModEffects.FREEZE.get(), duration, lvl));
                        }
                    }
                }
            }
            frostWalkerImitation(user, world, user.blockPosition(), 4);
        }
    }
    
    public static boolean turnSkeletonIntoStray(LivingEntity skeleton) {
        if (skeleton.level.isClientSide()) return false;
        ServerWorld world = (ServerWorld) skeleton.level;
        if ((world.getDifficulty() == Difficulty.NORMAL && skeleton.getRandom().nextBoolean() || world.getDifficulty() == Difficulty.HARD)) {
            StrayEntity stray = null;
            if (ForgeEventFactory.canLivingConvert(skeleton, EntityType.STRAY, (timer) -> {})) {
                stray = ((MobEntity) skeleton).convertTo(EntityType.STRAY, true);
            }
            else {
                return false;
            }
//            stray.finalizeSpawn(
//                    world, 
//                    world.getCurrentDifficultyAt(stray.blockPosition()), 
//                    SpawnReason.CONVERSION, 
//                    null, 
//                    null);
            ForgeEventFactory.onLivingConvert(skeleton, stray);
            if (!skeleton.isSilent()) {
                world.levelEvent(null, 1026, skeleton.blockPosition(), 0);
            }
            return true;
        }
        return false;
    }
    
    private void frostWalkerImitation(LivingEntity entity, World world, BlockPos entityPos, float radius) {
        if (entity.isOnGround()) {
            BlockState ice = Blocks.FROSTED_ICE.defaultBlockState();
            BlockPos.Mutable posMutable = new BlockPos.Mutable();
            for (BlockPos blockPos : BlockPos.betweenClosed(entityPos.offset(-radius, -1.0, -radius), entityPos.offset(radius, -1.0, radius))) {
                if (blockPos.closerThan(entity.position(), (double) radius)) {
                    posMutable.set(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
                    BlockState blockState = world.getBlockState(posMutable);
                    if (blockState.getBlock().isAir(blockState, world, posMutable)) {
                        blockState = world.getBlockState(blockPos);
                        boolean isFull = blockState.getBlock() == Blocks.WATER && blockState.getValue(FlowingFluidBlock.LEVEL) == 0;
                        if (blockState.getMaterial() == Material.WATER && isFull && ice.canSurvive(world, blockPos)
                                && world.isUnobstructed(ice, blockPos, ISelectionContext.empty())
                                && !ForgeEventFactory.onBlockPlace(entity, BlockSnapshot.create(world.dimension(), world, blockPos), Direction.UP)) {
                            world.setBlockAndUpdate(blockPos, ice);
                            world.getBlockTicks().scheduleTick(blockPos, Blocks.FROSTED_ICE, MathHelper.nextInt(entity.getRandom(), 20, 40));
                        }
                    }
                }
            }
        }
    }

    public static boolean onUserAttacked(LivingAttackEvent event) {
        Entity attacker = event.getSource().getDirectEntity();
        if (attacker instanceof LivingEntity && !attacker.isOnFire() && !DamageUtil.isImmuneToCold(attacker)) {
            LivingEntity targetLiving = event.getEntityLiving();
            return INonStandPower.getNonStandPowerOptional(targetLiving).map(power -> {
                if (power.getHeldAction(true) == ModActions.VAMPIRISM_FREEZE.get()) {
                    World world = attacker.level;
                    int difficulty = world.getDifficulty().getId();
                    ((LivingEntity) attacker).addEffect(new EffectInstance(ModEffects.FREEZE.get(), difficulty * 100, difficulty));
                    world.playSound(null, attacker, ModSounds.VAMPIRE_FREEZE.get(), attacker.getSoundSource(), 1.0F, 1.0F);
                    return true;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }
    
    @Override
    public double getMaxRangeSqEntityTarget() {
    	return 4;
    }

    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (requirementsFulfilled) {
            Vector3d particlePos = user.position().add(
                    (Math.random() - 0.5) * (user.getBbWidth() + 1.0), 
                    Math.random() * (user.getBbHeight() + 1.0), 
                    (Math.random() - 0.5) * (user.getBbWidth() + 1.0));
            user.level.addParticle(ParticleTypes.CLOUD, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
    }
    
    @Override
    public boolean heldAllowsOtherActions(INonStandPower power) {
        return true;
    }
}
