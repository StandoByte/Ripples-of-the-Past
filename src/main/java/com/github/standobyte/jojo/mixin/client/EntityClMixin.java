package com.github.standobyte.jojo.mixin.client;

import java.util.OptionalInt;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ControllerConsciousness;
import com.github.standobyte.jojo.client.ControllerStand;
import com.github.standobyte.jojo.client.IEntityGlowColor;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(Entity.class)
public class EntityClMixin implements IEntityGlowColor {
    private OptionalInt glowingColor = OptionalInt.empty();

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void jojoGlowingColor(CallbackInfoReturnable<Integer> ci) {
        glowingColor.ifPresent(ci::setReturnValue);
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    public void jojoSpecialGlowing(CallbackInfoReturnable<Boolean> ci) {
        if (glowingColor.isPresent()) {
            ci.setReturnValue(true);
        }
    }
    
    @Override
    public void setGlowColor(@Nonnull OptionalInt color) {
        this.glowingColor = color;
    }

    @Override
    public OptionalInt getGlowColor() {
        return glowingColor;
    }
    
    
    @Redirect(method = "isInvisibleTo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    public boolean jojoSpectatorVisibility(PlayerEntity player) {
        return JojoModUtil.seesInvisibleAsSpectator(player);
    }
    
    
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void jojoTurnRemoteStand(double yRot, double xRot, CallbackInfo ci) {
        if ((Object) this == ClientUtil.getClientPlayer()) {
            Entity splitCsns = ControllerConsciousness.getInstance().getCsnsEntity();
            if (splitCsns != null) {
                splitCsns.turn(yRot, xRot);
                ci.cancel();
                return;
            }
            
            StandEntity standManual = ControllerStand.getInstance().getManuallyControlledStand();
            if (standManual != null && standManual.isAlive()) {
                standManual.turn(yRot, xRot);
                ci.cancel();
                return;
            }
        }
    }
}
