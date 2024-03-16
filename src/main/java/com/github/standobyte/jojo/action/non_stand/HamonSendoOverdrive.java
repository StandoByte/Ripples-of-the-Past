package com.github.standobyte.jojo.action.non_stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.general.ObjectWrapper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonSendoOverdrive extends HamonAction {

    public HamonSendoOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void overrideVanillaMouseTarget(ObjectWrapper<ActionTarget> targetContainer, World world, LivingEntity user, INonStandPower power) {
        if (targetContainer.get().getType() == TargetType.BLOCK) {
            Vector3d pos1 = user.getEyePosition(1.0F);
            Vector3d pos2 = pos1.add(user.getViewVector(1.0F).scale(Math.sqrt(getMaxRangeSqBlockTarget())));
            RayTraceResult targetCollisionBlocks = user.level.clip(new RayTraceContext(
                    pos1, pos2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, user)); // to not target plant blocks like grass
            targetContainer.set(ActionTarget.fromRayTraceResult(targetCollisionBlocks));
        }
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        BlockPos blockPos = target.getBlockPos();
        Direction face = target.getFace();
        if (!user.level.getEntities(ModEntityTypes.SENDO_HAMON_OVERDRIVE.get(), new AxisAlignedBB(blockPos), 
                entity -> blockPos.equals(entity.getTargetedBlockPos()) && face == entity.getTargetedFace()).isEmpty()) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float energyCost = getEnergyCost(power, target);
            float hamonEfficiency = hamon.getActionEfficiency(energyCost, true);
            
            BlockPos blockPos = target.getBlockPos();
            HamonSendoOverdriveEntity sendoOverdrive = new HamonSendoOverdriveEntity(world, 
                    user, target.getFace().getAxis())
                    .setRadius((2 + hamon.getHamonControlLevelRatio() * 3) * hamonEfficiency)
                    .setWaveDamage(0.75F * hamonEfficiency)
                    .setWavesCount(2 + (int) ((2 + Math.min(hamon.getHamonControlLevelRatio() * 3, 2)) * hamonEfficiency))
                    .setStatPoints(Math.min(energyCost, power.getEnergy()) * hamonEfficiency);
            sendoOverdrive.moveTo(Vector3d.atCenterOf(blockPos).subtract(0, sendoOverdrive.getDimensions(null).height * 0.5, 0));
            sendoOverdrive.setBlockTarget(target.getBlockPos(), target.getFace());
            world.addFreshEntity(sendoOverdrive);
        }
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }
    
    @Nullable
    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
        if (!power.getUser().level.isClientSide() && !getTargetRequirement().checkTargetType(target.getType())) {
            return ModHamonActions.HAMON_OVERDRIVE.get().getVisibleAction(power, target);
        }
        return super.replaceAction(power, target);
    }
}
