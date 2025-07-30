package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.general.ObjectWrapper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonSendoOverdrive extends HamonAction {

    public HamonSendoOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
//    @Override
//    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
//        if (target.getEntity() instanceof LivingEntity && !getTargetRequirement().checkTargetType(target.getType())) {
//            return ModHamonActions.HAMON_OVERDRIVE.get().getVisibleAction(power, target);
//        }
//        return super.replaceAction(power, target);
//    }
    
    @Override
    public void overrideVanillaMouseTarget(ObjectWrapper<ActionTarget> targetContainer, World world, LivingEntity user, INonStandPower power) {
        ActionTarget target = targetContainer.get();
        if (target.getType() == TargetType.BLOCK) {
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
                Vector3d pos1 = user.getEyePosition(1.0F);
                Vector3d pos2 = pos1.add(user.getViewVector(1.0F).scale(Math.sqrt(getMaxRangeSqBlockTarget())));
                RayTraceResult targetCollisionBlocks = user.level.clip(new RayTraceContext(
                        pos1, pos2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, user)); // to not target plant blocks like grass
                targetContainer.set(ActionTarget.fromRayTraceResult(targetCollisionBlocks));
            }
        }
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
        ActionTarget target = power.getMouseTarget();
        if (target.getType() == TargetType.BLOCK) {
            if (!world.isClientSide()) {
                BlockPos blockPos = target.getBlockPos();
                Direction face = target.getFace();
                
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                float energyCost = getEnergyCost(power, target);
                float hamonEfficiency = hamon.getActionEfficiency(energyCost, false, getUnlockingSkill());
                
                HamonSendoOverdriveEntity sendoOverdrive = new HamonSendoOverdriveEntity(world, 
                        user, face.getAxis());
                float heldRatio = MathHelper.clamp((float) (power.getHeldActionTicks() - 1) / this.getHoldDurationToFire(power), 0, 1);
                sendoOverdrive.yRot = user.yRot;
                sendoOverdrive.xRot = user.xRot;
                sendoOverdrive.sparksAngle = (float) Math.PI / 4 + heldRatio * (float) Math.PI / 4 * 7;
                sendoOverdrive.radius = (2 + hamon.getHamonControlLevelRatio() * 3) * hamonEfficiency;
                sendoOverdrive.damage = 0.75F * hamonEfficiency;
                sendoOverdrive.setWavesCount(2 + (int) ((2 + Math.min(hamon.getHamonControlLevelRatio() * 3, 2)) * hamonEfficiency));
                sendoOverdrive.setStatPoints(Math.min(energyCost, power.getEnergy()) * hamonEfficiency);
                        
                sendoOverdrive.moveTo(Vector3d.atCenterOf(blockPos).subtract(0, sendoOverdrive.getDimensions(null).height * 0.5, 0));
                sendoOverdrive.setBlockTarget(target.getBlockPos(), target.getFace());
                world.addFreshEntity(sendoOverdrive);
                
                if (!willFire) power.consumeEnergy(energyCost);
            }
            user.swing(Hand.MAIN_HAND, false);
        }
    }
}
