package com.github.standobyte.jojo.mixin;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {
    @Shadow public abstract void shoot(double  $$1, double  $$2, double  $$3, float  $$4, float  $$5);

    @Shadow @Nullable public abstract Entity getOwner();
    @Unique
    public float ripples_of_the_Past$tsFlightTicks = 3;
    public ProjectileEntityMixin(EntityType<?> p_i48580_1_, World p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    //TODO Add smooth projectile slowdown

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void projectileTimestopTick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) this.getOwner();
        if (livingEntity != null) {
            if (TimeStopHandler.isTimeStopped(level, this.blockPosition())) {
                if (livingEntity instanceof StandEntity){
                    livingEntity = ((StandEntity)livingEntity).getUser();
                }
                if (TimeStopHandler.hasTimeStopAbility(livingEntity) && ripples_of_the_Past$tsFlightTicks > 0) {
                    ripples_of_the_Past$tsFlightTicks--;
                    super.canUpdate(true);
                }
                else {
                    super.canUpdate(false);
                }
            }
            else {
                ripples_of_the_Past$tsFlightTicks = 3;
            }
        }
    }
}