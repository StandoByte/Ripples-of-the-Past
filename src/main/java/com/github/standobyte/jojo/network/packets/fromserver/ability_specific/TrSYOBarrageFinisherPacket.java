package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.non_stand.HamonSunlightYellowOverdriveBarrage.SunlightYellowOverdriveInstance;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSYOBarrageFinisherPacket {
    private final int entityId;
    
    public TrSYOBarrageFinisherPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrSYOBarrageFinisherPacket> {

        @Override
        public void encode(TrSYOBarrageFinisherPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public TrSYOBarrageFinisherPacket decode(PacketBuffer buf) {
            return new TrSYOBarrageFinisherPacket(buf.readInt());
        }

        @Override
        public void handle(TrSYOBarrageFinisherPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getContinuousAction().ifPresent(action -> {
                        if (action instanceof SunlightYellowOverdriveInstance) {
                            ((SunlightYellowOverdriveInstance) action).startFinishingPunch();
                        }
                    });
                });
            }
        }

        @Override
        public Class<TrSYOBarrageFinisherPacket> getPacketClass() {
            return TrSYOBarrageFinisherPacket.class;
        }
    }

}
