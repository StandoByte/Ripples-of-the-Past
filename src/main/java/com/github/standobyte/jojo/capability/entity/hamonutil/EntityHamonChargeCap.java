package com.github.standobyte.jojo.capability.entity.hamonutil;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonEntityChargePacket;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonCharge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class EntityHamonChargeCap {
    private final Entity entity;
    private HamonCharge hamonCharge;
    private boolean clHasCharge = false;
    
    public EntityHamonChargeCap(Entity entity) {
        this.entity = entity;
    }
    
    public void tick() {
        if (!entity.canUpdate()) {
            return;
        }
        
        if (!entity.level.isClientSide()) {
            if (hamonCharge != null) {
                hamonCharge.tick(entity, null, entity.level, entity.getBoundingBox().inflate(1.0D));
                if (hamonCharge.shouldBeRemoved()) {
                    setHamonCharge(null);
                }
            }
        }
        
        else if (clHasCharge) {
            HamonSparksLoopSound.playSparkSound(entity, entity.getBoundingBox().getCenter(), 1.0F, true);
            CustomParticlesHelper.createHamonSparkParticles(entity, entity.getRandomX(0.5), entity.getY(Math.random()), entity.getRandomZ(0.5), 1);
        }
    }
    
    
    
    public void setHamonCharge(float tickDamage, int chargeTicks, LivingEntity hamonUser, float energySpent) {
        setHamonCharge(new HamonCharge(tickDamage, chargeTicks, hamonUser, energySpent));
    }
    
    public void setHamonCharge(HamonCharge charge) {
        this.hamonCharge = charge;
        if (!entity.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(TrHamonEntityChargePacket.entityCharge(entity.getId(), hamonCharge != null), entity);
        }
    }
    
    public boolean hasHamonCharge() {
        return !entity.level.isClientSide() ? hamonCharge != null && !hamonCharge.shouldBeRemoved() : clHasCharge;
    }
    
    @Nullable
    public HamonCharge getHamonCharge() {
        return hasHamonCharge() ? hamonCharge : null;
    }
    
    public void onTracking(ServerPlayerEntity tracking) {
        boolean hasCharge = hasHamonCharge();
        if (hasCharge) {
            PacketManager.sendToClient(TrHamonEntityChargePacket.entityCharge(entity.getId(), hasCharge), tracking);
        }
    }
    
    public void setClSideHasCharge(boolean hasCharge) {
        this.clHasCharge = hasCharge;
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (hamonCharge != null) {
            nbt.put("HamonCharge", hamonCharge.toNBT());
        }
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("HamonCharge", 10)) {
            hamonCharge = HamonCharge.fromNBT(nbt.getCompound("HamonCharge"));
        }
    }
}
