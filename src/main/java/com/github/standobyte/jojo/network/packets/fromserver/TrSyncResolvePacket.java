package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSyncResolvePacket {
    private final int entityId;
    private final float resolve;
    private final int noDecayTicks;
    
    public TrSyncResolvePacket(int entityId, float resolve, int noDecayTicks) {
        this.entityId = entityId;
        this.resolve = resolve;
        this.noDecayTicks = noDecayTicks;
    }
    
    public static void encode(TrSyncResolvePacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.resolve);
        buf.writeVarInt(msg.noDecayTicks);
    }
    
    public static TrSyncResolvePacket decode(PacketBuffer buf) {
        return new TrSyncResolvePacket(buf.readInt(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(TrSyncResolvePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IStandPower.getStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    if (power.isInResolveMode() && power.getNoResolveDecayTicks() < msg.noDecayTicks) {
                        // 
                    }
                    power.setResolve(msg.resolve, msg.noDecayTicks);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
