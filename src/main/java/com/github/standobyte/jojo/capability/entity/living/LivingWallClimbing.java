package com.github.standobyte.jojo.capability.entity.living;

import java.util.Optional;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonWallClimbingPacket;
import com.github.standobyte.jojo.util.general.OptionalFloat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;

public class LivingWallClimbing implements INBTSerializable<CompoundNBT> {
    private final LivingEntity entity;
    private boolean wallClimbing = false;
    private OptionalFloat wallClimbBodyRot = OptionalFloat.empty();
    public boolean wallClimbIsMoving;
    private boolean wallClimbHamon = false;
    private float wallClimbSpeed = 0;
    
    public LivingWallClimbing(LivingEntity entity) {
        this.entity = entity;
    }
    
    public boolean isWallClimbing() {
        return wallClimbing;
    }
    
    public boolean isHamon() {
        return wallClimbHamon;
    }
    
    public float getWallClimbSpeed() {
        if (wallClimbHamon) {
            return (float) ModHamonActions.HAMON_WALL_CLIMBING.get().getHamonWallClimbSpeed(entity);
        }
        return wallClimbSpeed;
    }
    
    public OptionalFloat getWallClimbYRot() {
        return wallClimbBodyRot;
    }
    
    public void setWallClimbYRot(OptionalFloat yRot) {
        this.wallClimbBodyRot = yRot;
    }
    
    public void stopWallClimbing() {
        setWallClimbing(false, false, 0, OptionalFloat.empty());
    }
    
    public void setWallClimbing(boolean value, boolean hamon, float climbSpeed, OptionalFloat yBodyRot) {
        this.wallClimbing = value;
        this.wallClimbHamon = hamon;
        this.wallClimbBodyRot = yBodyRot;
        if (!entity.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrHamonWallClimbingPacket(
                    entity.getId(), wallClimbing, hamon, climbSpeed, yBodyRot), entity);
        }
        else if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (!value) {
                if (player.isLocalPlayer()) {
                    ClientUtil.setPlayerHandsBusy(player, false);
                }
            }
            ModPlayerAnimations.wallClimbing.setAnimEnabled(player, value);
        }
    }
    
//    public void startWallPullUp() {
//        
//    }
    
    public void climbLimitPlayerHeadRot() {
        if (wallClimbing && wallClimbBodyRot.isPresent()) {
            float climbYRot = -wallClimbBodyRot.getAsFloat();
            
            entity.setYBodyRot(climbYRot);
            entity.yBodyRotO = entity.yBodyRot;
            float f = MathHelper.wrapDegrees(entity.yRot - climbYRot);
            float f1 = MathHelper.clamp(f, -75, 75);
            entity.yRotO += f1 - f;
            entity.yRot += f1 - f;
            entity.setYHeadRot(entity.yRot);
        }
    }
    
    
    public void syncToPlayer(ServerPlayerEntity tracking) {
        if (wallClimbing) {
            PacketManager.sendToClient(new TrHamonWallClimbingPacket(
                    entity.getId(), wallClimbing, wallClimbHamon, wallClimbSpeed, wallClimbBodyRot), tracking);
        }
    }
    
    
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("WallClimb", wallClimbing);
        nbt.putBoolean("WallClimbHamon", wallClimbHamon);
        nbt.putFloat("WallClimbSpeed", wallClimbSpeed);
        if (wallClimbBodyRot.isPresent()) {
            nbt.putFloat("WallClimbRot", wallClimbBodyRot.getAsFloat());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        wallClimbing = nbt.getBoolean("WallClimb");
        wallClimbHamon = nbt.getBoolean("WallClimbHamon");
        wallClimbSpeed = nbt.getFloat("WallClimbSpeed");
        if (nbt.contains("WallClimbRot")) {
            wallClimbBodyRot = OptionalFloat.of(nbt.getFloat("WallClimbRot"));
        }
    }
    
    
    public static Optional<LivingWallClimbing> getHandler(LivingEntity entity) {
        return entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.getWallClimbHandler());
    }
}
