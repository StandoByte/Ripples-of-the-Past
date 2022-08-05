package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondBlockCheckpointMove extends StandEntityAction {

    public CrazyDiamondBlockCheckpointMove(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack blockItem = user.getOffhandItem();
        if (blockItem == null || blockItem.isEmpty() || blockItem.getTag() == null || !blockItem.getTag().contains("CDCheckpoint")) {
            return conditionMessage("item_block_origin");
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = userPower.getUser();
        if (user != null) {
            ItemStack heldItem = user.getOffhandItem();
            CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(world, heldItem).ifPresent(pos -> {
                Vector3d posD = Vector3d.atCenterOf(pos);
                if (user.distanceToSqr(posD) > 16) {
                    user.setDeltaMovement(posD.subtract(user.position()).normalize().scale(0.75));
                    user.fallDistance = 0;
                }
                else {
                    // FIXME !!!!!!!!!! place the block
                }
                if (world.isClientSide()) {
                    // FIXME !!!!!!!!!! CD restore sound
                    CustomParticlesHelper.createCDRestorationParticle(user, Hand.OFF_HAND);
                }
            });
        }
    }
    
}
