package com.github.standobyte.jojo.power.impl.stand;

import java.util.UUID;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.JojoModConfig.Common;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ArrowXpLevelsDataPacket;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;

public class StandArrowHandler {
    private int xpLevelsTakenByArrow;
    private int standsGotFromArrow;
    private UUID standArrowShooterUUID;
    private boolean healStandArrowDamage;
    
    
    
    public void tick(LivingEntity user) {
        if (healStandArrowDamage) {
            float health = user.getHealth();
            if (health < user.getMaxHealth()) {
                user.heal(0.25F);
            }
            else {
                healStandArrowDamage = false;
            }
        }
    }
    
    public void clear() {
        this.standsGotFromArrow = 0;
    }
    
    public void keepOnDeath(StandArrowHandler handler) {
        this.standsGotFromArrow = handler.standsGotFromArrow;
    }
    
    public void syncWithUser(ServerPlayerEntity user) {
        PacketManager.sendToClient(new ArrowXpLevelsDataPacket(xpLevelsTakenByArrow, standsGotFromArrow), user);
    }
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("ArrowLevels", xpLevelsTakenByArrow);
        nbt.putInt("ArrowStands", standsGotFromArrow);
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        xpLevelsTakenByArrow = nbt.getInt("ArrowLevels");
        standsGotFromArrow = nbt.getInt("ArrowStands");
    }
    
    
    
    public int decXpLevelsTakenByArrow(LivingEntity user) {
        setXpLevelsTakenByArrow(this.xpLevelsTakenByArrow + 1, user);
        return this.xpLevelsTakenByArrow;
    }
    
    public void setXpLevelsTakenByArrow(int levels, LivingEntity user) {
        this.xpLevelsTakenByArrow = levels;
        if (user instanceof ServerPlayerEntity) {
            PacketManager.sendToClient(new ArrowXpLevelsDataPacket(xpLevelsTakenByArrow, standsGotFromArrow), (ServerPlayerEntity) user);
        }
    }
    
    public int getXpLevelsTakenByArrow() {
        return xpLevelsTakenByArrow;
    }
    
    public int getStandXpLevelsRequirement(boolean clientSide) {
        Common config = JojoModConfig.getCommonConfigInstance(clientSide);
        return config.standXpCostInitial.get() + standsGotFromArrow * config.standXpCostIncrease.get();
    }
    
    public void onGettingStandFromArrow(LivingEntity user) {
        xpLevelsTakenByArrow = 0;
        standsGotFromArrow++;
        if (!user.level.isClientSide()) {
            if (user instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(new ArrowXpLevelsDataPacket(xpLevelsTakenByArrow, standsGotFromArrow), (ServerPlayerEntity) user);
            }

            if (standArrowShooterUUID != null) {
                PlayerEntity shooter = ((ServerWorld) user.level).getPlayerByUUID(standArrowShooterUUID);
                if (shooter != null) {
                    ModCriteriaTriggers.STAND_ARROW_HIT.get().trigger((ServerPlayerEntity) shooter, user, true);
                }
                standArrowShooterUUID = null;
            }
            
            healStandArrowDamage = true;
        }
    }
    
    public void setStandArrowShooter(LivingEntity shooter) {
        this.standArrowShooterUUID = shooter.getUUID();
    }
    
    public void setFromPacket(ArrowXpLevelsDataPacket packet) {
        this.xpLevelsTakenByArrow = packet.levels;
        this.standsGotFromArrow = packet.gotStands;
    }

}
