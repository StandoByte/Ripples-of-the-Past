package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class NotificationSyncPacket {
    private final Set<OneTimeNotification> notifSent;
    
    public NotificationSyncPacket(Set<OneTimeNotification> notifSent) {
        this.notifSent = notifSent;
    }
    
    
    public static class Handler implements IModPacketHandler<NotificationSyncPacket> {

        @Override
        public void encode(NotificationSyncPacket msg, PacketBuffer buf) {
            long encoded = 0;
            long mask = 1 << OneTimeNotification.values().size();
            for (OneTimeNotification flag : OneTimeNotification.values()) {
                if (msg.notifSent.contains(flag)) {
                    encoded |= mask;
                }
                encoded >>= 1;
            }
            buf.writeVarLong(encoded);
        }

        @Override
        public NotificationSyncPacket decode(PacketBuffer buf) {
            long encoded = buf.readVarLong();
            Set<OneTimeNotification> notifSent = new HashSet<>();
            for (OneTimeNotification flag : OneTimeNotification.values()) {
                if ((encoded & 1) > 0) {
                    notifSent.add(flag);
                }
                encoded >>= 1;
            }
            return new NotificationSyncPacket(notifSent);
        }

        @Override
        public void handle(NotificationSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientUtil.getClientPlayer().getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                for (OneTimeNotification flag : OneTimeNotification.values()) {
                    cap.setSentNotification(flag, msg.notifSent.contains(flag));
                }
            });
        }

        @Override
        public Class<NotificationSyncPacket> getPacketClass() {
            return NotificationSyncPacket.class;
        }
    }
}
