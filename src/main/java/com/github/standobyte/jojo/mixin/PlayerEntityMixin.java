package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void jojoPlayerUndeadCreature(CallbackInfoReturnable<CreatureAttribute> ci) {
        if (JojoModUtil.playerUndeadAttribute((LivingEntity) (Object) this)) {
            ci.setReturnValue(CreatureAttribute.UNDEAD);
        }
    }
    
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void jojoPlayerWallClimb(Vector3d pTravelVector, CallbackInfo ci) {
        PlayerEntity thisPlayer = (PlayerEntity) (Object) this;
        if (HamonWallClimbing2.travelWallClimb(thisPlayer, pTravelVector)) {
            ci.cancel();
        }
    }
}
