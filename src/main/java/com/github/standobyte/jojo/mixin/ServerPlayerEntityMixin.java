package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }
    
    @Inject(method = "doTick", at = @At("HEAD"), cancellable = true)
    public void jojoTsCancelPlayerTick(CallbackInfo ci) {
        if (!this.canUpdate()) {
            ci.cancel();
        }
    }
    
    
    @Inject(method = "teleportTo(Lnet/minecraft/world/server/ServerWorld;DDDFF)V", at = @At("HEAD"), cancellable = true)
    public void jojoCancelTeleport(ServerWorld pNewLevel, double pX, double pY, double pZ, float pYaw, float pPitch, CallbackInfo ci) {
        if (this instanceof IPlayerPossess) {
            IPlayerPossess player = (IPlayerPossess) this;
            Entity possessedEntity = player.jojoGetPossessedEntity();
            if (possessedEntity != null) {
                ci.cancel();
            }
        }
    }
    
    @Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
    public void jojoCancelEntitySpectate(Entity entityToSpectate, CallbackInfo ci) {
        if (this instanceof IPlayerPossess) {
            IPlayerPossess player = (IPlayerPossess) this;
            Entity possessedEntity = player.jojoGetPossessedEntity();
            // TODO disable this, only allow using specific actions to un-possess an entity
            if (possessedEntity != null && possessedEntity != entityToSpectate) {
                if (player.jojoIsPossessingAsAlive() && entityToSpectate == this) {
                    player.jojoPossessEntity(null, true, null);
                }
                else {
                    ci.cancel();
                }
            }
        }
    }

}
