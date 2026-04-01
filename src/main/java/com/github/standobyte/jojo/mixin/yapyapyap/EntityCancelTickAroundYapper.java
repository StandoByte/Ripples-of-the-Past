package com.github.standobyte.jojo.mixin.yapyapyap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.mechanics.speechbubble.clowning.WorldTypingPlayers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class EntityCancelTickAroundYapper {
    @Shadow public World level;
    @Shadow public abstract Vector3d position();
    
    @Inject(method = "canUpdate()Z", at = @At("HEAD"), cancellable = true, remap = false)
    public void canUpdateCheckYapper(CallbackInfoReturnable<Boolean> ci) {
        boolean isImmune = false;
        if ((Object) this instanceof PlayerEntity) {
            PlayerEntity asPlayer = (PlayerEntity) (Object) this;
            isImmune = !WorldTypingPlayers.isTyping(asPlayer) && (asPlayer.isSpectator() || asPlayer.isCreative());
        }
        if (!isImmune && WorldTypingPlayers.hasInRange(level, this.position())) {
            ci.setReturnValue(false);
        }
    }
}
