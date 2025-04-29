package com.github.standobyte.jojo.capability.entity.living;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class LivingStuckObjects implements INBTSerializable<CompoundNBT> {
    private final LivingEntity entity;
    private final StuckObjectsTracker knives;
    
    public LivingStuckObjects(LivingEntity entity) {
        this.entity = entity;
        this.knives = new StuckObjectsTracker(entity, Type.KNIFE);
    }
    
    public StuckObjectsTracker getKnives() {
        return knives;
    }
    
    public void tick() {
        if (!entity.level.isClientSide()) {
            knives.tick();
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        knives.toNBT(nbt, "Knives");
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        knives.fromNBT(nbt, "Knives");
    }
    
    public void onTracking(ServerPlayerEntity tracking) {
        knives.onTracking(tracking);
    }
    
    public void syncWithClient() {
        if (entity instanceof ServerPlayerEntity) {
            knives.syncWithPlayerEntity((ServerPlayerEntity) entity);
        }
    }
    
    
    
    public static enum Type {
        KNIFE
    }
    
    public class StuckObjectsTracker {
        private final Type type;
        private final LivingEntity entity;
        private int count;
        private int removeTime;
        
        public StuckObjectsTracker(LivingEntity entity, Type type) {
            this.entity = entity;
            this.type = type;
        }
        
        public int getCount() {
            return count;
        }
        
        public void tick() {
            if (count > 0) {
                if (removeTime <= 0) {
                    removeTime = 20 * (30 - count);
                }
                removeTime--;
                if (removeTime <= 0) {
                    setCount(count - 1);
                }
            }
        }
        
        public void increment() {
            setCount(count + 1);
        }
        
        public void setCount(int count) {
            count = Math.max(count, 0);
            if (this.count != count) {
                this.count = count;
                if (!entity.level.isClientSide()) {
                    PacketManager.sendToClientsTrackingAndSelf(makePacket(), entity);
                }
            }
        }
        
        
        public void toNBT(CompoundNBT mainNbt, String key) {
            mainNbt.putInt(key, count);
        }
        
        public void fromNBT(CompoundNBT mainNbt, String key) {
            count = mainNbt.getInt(key);
        }
        
        
        public void onTracking(ServerPlayerEntity tracking) {
            PacketManager.sendToClient(makePacket(), tracking);
        }
        
        public void syncWithPlayerEntity(ServerPlayerEntity entityAsPlayer) {
            PacketManager.sendToClient(makePacket(), entityAsPlayer);
        }
        
        public Object makePacket() {
            switch (type) {
            case KNIFE:
                return new TrKnivesCountPacket(entity.getId(), count);
            default:
                throw new AssertionError();
            }
        }
    }
}
