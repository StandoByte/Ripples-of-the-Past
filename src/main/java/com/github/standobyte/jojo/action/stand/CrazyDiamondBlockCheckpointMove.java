package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondBlockCheckpointMove extends StandAction {

    public CrazyDiamondBlockCheckpointMove(AbstractBuilder<?> builder) {
        super(builder);
    }

    @Override
    protected void holdTick(World world, LivingEntity user, IStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        ItemStack heldItem = user.getMainHandItem();
        CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(world, user, heldItem).ifPresent(pos -> {
            Vector3d posD = Vector3d.atCenterOf(pos);
            if (user.distanceToSqr(posD) > 16) {
                user.setDeltaMovement(posD.subtract(user.position()).normalize().scale(0.75));
                user.fallDistance = 0;
            }
            else {
                // FIXME !!!!!!!!!! place the block
            }
            if (world.isClientSide()) {
                Vector3d handPos = user.position().add(new Vector3d(
                        user.getBbWidth() * 0.75 * (user.getMainArm() == HandSide.RIGHT ? -1 : 1), 
                        user.getBbHeight() * 0.4, user.getBbWidth() * 0.5)
                        .yRot(-user.yBodyRot * MathUtil.DEG_TO_RAD));
                // FIXME !!!!!!!!!! CD restore particles
                // FIXME !!!!!!!!!! btw particles should move with the player
                // FIXME !!!!!!!!!! CD restore sound
                world.addParticle(ParticleTypes.CRIT, handPos.x, handPos.y, handPos.z, 0, 0, 0);
            }
        });
    }
    
}
