package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonEntityChargePacket {
    private final int entityId;
    private final Type type;
    public final boolean hasCharge;
    public final int tickCount;
    public final int maxTicks;
    
    public static TrHamonEntityChargePacket entityCharge(int entityId, boolean hasCharge) {
        return new TrHamonEntityChargePacket(entityId, hasCharge, Type.ENTITY, -1, -1);
    }
    
    public static TrHamonEntityChargePacket projectileCharge(int entityId, boolean hasCharge, int tickCount, int maxTicks) {
        return new TrHamonEntityChargePacket(entityId, true, Type.PROJECTILE, tickCount, maxTicks);
    }
    
    private TrHamonEntityChargePacket(int entityId, boolean hasCharge, Type type, int tickCount, int maxTicks) {
        this.entityId = entityId;
        this.hasCharge = hasCharge;
        this.type = type;
        this.tickCount = tickCount;
        this.maxTicks = maxTicks;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonEntityChargePacket> {
    
        public void encode(TrHamonEntityChargePacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.type);
            buf.writeInt(msg.entityId);
            buf.writeBoolean(msg.hasCharge);
            switch (msg.type) {
            case PROJECTILE:
                if (msg.hasCharge) {
                    buf.writeVarInt(msg.tickCount);
                    buf.writeVarInt(msg.maxTicks);
                }
                break;
            default:
                break;
            }
        }
        
        public TrHamonEntityChargePacket decode(PacketBuffer buf) {
            Type type = buf.readEnum(Type.class);
            int entityId = buf.readInt();
            boolean hasCharge = buf.readBoolean();
            switch (type) {
            case ENTITY:
                return entityCharge(entityId, hasCharge);
            case PROJECTILE:
                int ticks = -1;
                int ticksMax = -1;
                if (hasCharge) {
                    ticks = buf.readVarInt();
                    ticksMax = buf.readVarInt();
                }
                return projectileCharge(entityId, hasCharge, ticks, ticksMax);
            default:
                throw new NoSuchElementException(type.name() + " Hamon charge type isn't handled");
            }
        }
    
        public void handle(TrHamonEntityChargePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                switch (msg.type) {
                case ENTITY:
                    entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.setClSideHasCharge(msg.hasCharge);
                    });
                    break;
                case PROJECTILE:
                    entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.handleMsgFromServer(msg);
                    });
                    break;
                }
            }
        }

        @Override
        public Class<TrHamonEntityChargePacket> getPacketClass() {
            return TrHamonEntityChargePacket.class;
        }
    }
    
    private static enum Type {
        ENTITY,
        PROJECTILE;
    }
}
