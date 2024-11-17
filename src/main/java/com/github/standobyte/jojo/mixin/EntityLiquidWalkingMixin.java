package com.github.standobyte.jojo.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojo.action.non_stand.HamonLiquidWalking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * This class was created by <b>florensie</b>. It's distributed as
 * part of the Expandability library mod. Get the Source Code on GitHub:
 * <a href="https://github.com/florensie/ExpandAbility">https://github.com/florensie/ExpandAbility</a>.
 * <br>
 * ExpandAbility is open-source and distributed under the MIT license.
 */
@Mixin(Entity.class)
public class EntityLiquidWalkingMixin {

    /**
     * Adjusts an entity's movement to account for fluid walking. This ensures that the player never actually touches
     * any of the fluids they can walk on.
     *
     * @param originalDisplacement the entity's proposed displacement accounting for collisions
     * @return a new Vector3dd representing the displacement after fluid walking is accounted for
     */
    @ModifyVariable(method = "move", ordinal = 1, index = 3, at = @At(
            value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/Entity;collide(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;"))
    private Vector3d fluidCollision(Vector3d originalDisplacement) {
        // We only support living entities
        //noinspection ConstantConditions
        if (!((Object) this instanceof LivingEntity)) {
            return originalDisplacement;
        }

        LivingEntity entity = (LivingEntity) (Object) this;

        // A bunch of checks to see if fluid walking is even possible
        if (originalDisplacement.y <= 0.0 && !isTouchingFluid(entity,entity.getBoundingBox().deflate(0.001D))) {
            Map<Vector3d, Double> points = findFluidDistances(entity, originalDisplacement);
            Double highestDistance = null;

            for (Map.Entry<Vector3d, Double> point : points.entrySet()) {
                if (highestDistance == null || (point.getValue() != null && point.getValue() > highestDistance)) {
                    highestDistance = point.getValue();
                }
            }

            if (highestDistance != null) {
                Vector3d finalDisplacement = new Vector3d(originalDisplacement.x, highestDistance, originalDisplacement.z);
                AxisAlignedBB finalBox = entity.getBoundingBox().move(finalDisplacement).deflate(0.001D);
                if (isTouchingFluid(entity, finalBox)) {
                    return originalDisplacement;
                } else {
                    entity.fallDistance = 0.0F;
                    entity.setOnGround(true);
                    return finalDisplacement;
                }
            }
        }

        return originalDisplacement;
    }

    /**
     * Gives the entity's distance to various fluids underneath (and above) it, in terms of the four bottom points
     * of its bounding box.
     *
     * @param entity the entity to check for fluid walking.
     * @param originalDisplacement the entity's proposed displacement after checking for collisions.
     * @return a map containing each bottom corner of the entity's original displaced bounding box, and the distance
     * between that corner and a given fluid, with a value of null for points with no fluid in range.
     */
    @Unique
    private static Map<Vector3d, Double> findFluidDistances(LivingEntity entity, Vector3d originalDisplacement) {
        AxisAlignedBB box = entity.getBoundingBox().move(originalDisplacement);

        HashMap<Vector3d, Double> points = new HashMap<>();
        points.put(new Vector3d(box.minX, box.minY, box.minZ), null);
        points.put(new Vector3d(box.minX, box.minY, box.maxZ), null);
        points.put(new Vector3d(box.maxX, box.minY, box.minZ), null);
        points.put(new Vector3d(box.maxX, box.minY, box.maxZ), null);

        double fluidStepHeight = entity.isOnGround() ? Math.max(1.0, entity.maxUpStep) : 0.0;

        for (Map.Entry<Vector3d, Double> entry : points.entrySet()) {
            for (int i = 0; ; i--) { // Check successive blocks downward
                // Auto step is essentially just shifting the fall adjustment up by the step height
                BlockPos landingPos = new BlockPos(entry.getKey()).offset(0.0, i + fluidStepHeight, 0.0);
                FluidState landingState = entity.getCommandSenderWorld().getFluidState(landingPos);

                double distanceToFluidSurface = landingPos.getY() + landingState.getOwnHeight() - entity.getY();
                double limitingVelocity = originalDisplacement.y;

                if (distanceToFluidSurface < limitingVelocity || distanceToFluidSurface > fluidStepHeight) {
                    break;
                }

                if (!landingState.isEmpty() && HamonLiquidWalking.onLiquidWalkingEvent(entity, landingState)) {
                    entry.setValue(distanceToFluidSurface);
                    break;
                }
            }
        }

        return points;
    }

    /**
     * Checks if a given entity's bounding box is touching any fluids. This is modified vanilla code that works for
     * any fluid, since vanilla only has one for water.
     *
     * @param entity the entity to check for fluid walking
     * @param box the entity's proposed bounding box to check
     * @return whether the entity's proposed bounding box will touch any fluids
     */
    @Unique
    private static boolean isTouchingFluid(LivingEntity entity, AxisAlignedBB box) {
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);
        World world = entity.getCommandSenderWorld();

        //noinspection deprecation
        if (world.hasChunksAt(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            // Loop over coords in bounding box
            for (int i = minX; i < maxX; ++i) {
                for (int j = minY; j < maxY; ++j) {
                    for (int k = minZ; k < maxZ; ++k) {
                        mutable.set(i, j, k);
                        FluidState fluidState = world.getFluidState(mutable);

                        if (!fluidState.isEmpty()) {
                            double surfaceY = fluidState.getHeight(world, mutable) + j;

                            if (surfaceY >= box.minY) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
