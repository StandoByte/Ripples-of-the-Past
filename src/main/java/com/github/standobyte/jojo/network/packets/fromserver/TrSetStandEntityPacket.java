package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSetStandEntityPacket {
    private final int userId;
    private final int standEntityId;

    public TrSetStandEntityPacket(int userId, int standEntityId) {
        this.userId = userId;
        this.standEntityId = standEntityId;
    }

    public static void encode(TrSetStandEntityPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.userId);
        buf.writeInt(msg.standEntityId);
    }

    public static TrSetStandEntityPacket decode(PacketBuffer buf) {
        return new TrSetStandEntityPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(TrSetStandEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity userEntity = ClientUtil.getEntityById(msg.userId);
            if (userEntity instanceof LivingEntity) {
                LivingEntity userLiving = (LivingEntity) userEntity;
                IStandPower.getStandPowerOptional(userLiving).ifPresent(power -> {
                    if (msg.standEntityId < 0) {
                        power.setStandManifestation(null);
                    }
                    else {
                        Entity entity = ClientUtil.getEntityById(msg.standEntityId);
                        if (entity instanceof StandEntity) {
                            StandEntity stand = (StandEntity) entity;
                            power.setStandManifestation(stand);
                        }
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
