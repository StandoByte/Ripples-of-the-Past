package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World level;

//    @Shadow private int portalCooldown;
//    @Shadow protected int portalTime;
    
    private KnockbackCollisionImpact jojoKbCollision;
    private boolean repeatCollide;
    
    @Inject(method = "collide", at = @At("TAIL"), cancellable = true)
    public void jojoCollideBreakBlocks(Vector3d movementVec, CallbackInfoReturnable<Vector3d> ci) {
        if (repeatCollide) {
            repeatCollide = false;
            return;
        }
        if (jojoKbCollision == null) {
            jojoKbCollision = KnockbackCollisionImpact.getHandler((Entity) (Object) this).orElse(null);
        }
        if (jojoKbCollision != null) {
            if (jojoKbCollision.collideBreakBlocks(movementVec, ci.getReturnValue(), level)) {
                repeatCollide = true;
                Vector3d repeatCollide = collide(movementVec);
                ci.setReturnValue(repeatCollide);
            }
        }
    }
    
    @Shadow
    protected abstract Vector3d collide(Vector3d pVec);
    
    
    
    @Redirect(method = "updateFluidHeightAndDoFluidPushing", at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/fluid/FluidState;getFlow("
                    + "Lnet/minecraft/world/IBlockReader;"
                    + "Lnet/minecraft/util/math/BlockPos;)"
                    + "Lnet/minecraft/util/math/vector/Vector3d;"))
    public Vector3d jojoTsCancelFluidPush(FluidState fluidState, IBlockReader world, BlockPos blockPos) {
        if (TimeStopHandler.isTimeStopped(level, blockPos)) {
            return Vector3d.ZERO;
        }
        return fluidState.getFlow(world, blockPos);
    }
}
