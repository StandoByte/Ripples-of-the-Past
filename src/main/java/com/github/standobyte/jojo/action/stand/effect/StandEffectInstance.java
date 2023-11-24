package com.github.standobyte.jojo.action.stand.effect;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEffectPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class StandEffectInstance {
    @Nonnull public final StandEffectType<?> effectType;
    
    private int id;
    public int tickCount = 0;
    private boolean toBeRemoved = false;
    
    protected LivingEntity user;
    protected World world;
    protected IStandPower userPower;
    
    private LivingEntity target;
    private UUID targetUUID;
    private int targetNetworkId = -1;
    
    
    public StandEffectInstance(@Nonnull StandEffectType<?> effectType) {
        this.effectType = effectType;
    }
    
    public StandEffectInstance withUser(LivingEntity user) {
        this.user = user;
        this.world = user.level;
        this.userPower = IStandPower.getStandPowerOptional(user).orElse(null);
        return this;
    }
    
    public StandEffectInstance withStand(IStandPower stand) {
        this.user = stand.getUser();
        this.world = user.level;
        this.userPower = stand;
        return this;
    }
    
    public StandEffectInstance withId(int id) {
        this.id = id;
        return this;
    }
    
    public StandEffectInstance withTarget(LivingEntity target) {
        this.target = target;
        this.targetUUID = target != null ? target.getUUID() : null;
        this.targetNetworkId = target.getId();
        return this;
    }
    
    public StandEffectInstance withTargetEntityId(int entityId) {
        this.targetNetworkId = entityId;
        if (target != null && target.getId() != entityId) {
            this.target = null;
        }
        return this;
    }
    
    public LivingEntity getStandUser() {
        return user;
    }
    
    public LivingEntity getTarget() {
        return target;
    }
    
    public UUID getTargetUUID() {
        return targetUUID;
    }
    
    public void onStart() {
        if (target != null) {
            target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.addEffectTargetedBy(this));
        }
        start();
    }
    
    public void onTick() {
        tickCount++;

        updateTarget(world);
        
        if (!world.isClientSide() && targetUUID == null && needsTarget()) {
            remove();
            return;
        }
        
        tick();
    }

    public void updateTarget(World world) {
        if (target == null) {
            if (!world.isClientSide()) {
                if (targetUUID != null) {
                    Entity entity = ((ServerWorld) world).getEntity(targetUUID);
                    if (entity instanceof LivingEntity) {
                        setTarget((LivingEntity) entity);
                        PacketManager.sendToClientsTrackingAndSelf(TrStandEffectPacket.updateTarget(this), user);
                    }
                }
            }
            else if (targetNetworkId > -1) {
                Entity entity = world.getEntity(targetNetworkId);
                if (entity instanceof LivingEntity) {
                    setTarget((LivingEntity) entity);
                }
            }
        }
        
        if (!world.isClientSide() && targetUUID != null && target != null) {
            if (!keepTarget(target)) {
                targetUUID = null;
                setTarget(null);
                PacketManager.sendToClientsTrackingAndSelf(TrStandEffectPacket.updateTarget(this), user);
            }
        }
        
        if (target != null) {
            if (!target.isAlive()) {
                setTarget(null);
            }
            else {
                tickTarget(target);
            }
        }
    }
    
    protected void setTarget(LivingEntity target) {
        if (this.target != target) {
            if (this.target != null) {
                this.target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.removeEffectTargetedBy(this));
            }
            if (target != null) {
                target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.addEffectTargetedBy(this));
            }
            this.target = target;
        }
    }
    
    public void onStop() {
        if (target != null) {
            target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.removeEffectTargetedBy(this));
        }
        stop();
    }
    
    protected abstract void start();
    protected abstract void tickTarget(LivingEntity target);
    protected abstract void tick();
    protected abstract void stop();
    
    public boolean removeOnUserDeath() {
        return true;
    }
    
    public boolean removeOnUserLogout() {
        return true;
    }
    
    public boolean removeOnStandChanged() {
        return true;
    }
    
    protected boolean keepTarget(LivingEntity target) {
        return !target.isDeadOrDying();
    }
    
    protected abstract boolean needsTarget();
    
    public int getId() {
        return id;
    }
    
    public void remove() {
        toBeRemoved = true;
    }
    
    public boolean toBeRemoved() {
        return toBeRemoved;
    }
    
    public void syncWithUserOnly(ServerPlayerEntity user) {
        updateTarget(user.level);
    }
    
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        PacketManager.sendToClient(TrStandEffectPacket.add(this), player);
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("Type", effectType.getRegistryName().toString());
        nbt.putInt("TickCount", tickCount);
        if (targetUUID != null) {
            nbt.putUUID("Target", targetUUID);
        }
        
        writeAdditionalSaveData(nbt);
        return nbt;
    }
    
    public static StandEffectInstance fromNBT(CompoundNBT nbt) {
        StandEffectType<?> effectType = JojoCustomRegistries.STAND_EFFECTS.getRegistry().getValue(new ResourceLocation(nbt.getString("Type")));
        if (effectType == null) return null;
        StandEffectInstance effect = effectType.create();
        effect.tickCount = nbt.getInt("TickCount");
        if (nbt.hasUUID("Target")) {
            effect.targetUUID = nbt.getUUID("Target");
        }
        
        effect.readAdditionalSaveData(nbt);
        return effect;
    }
    
    public void writeAdditionalPacketData(PacketBuffer buf) {}
    
    public void readAdditionalPacketData(PacketBuffer buf) {}

    protected void writeAdditionalSaveData(CompoundNBT nbt) {}

    protected void readAdditionalSaveData(CompoundNBT nbt) {}
}
