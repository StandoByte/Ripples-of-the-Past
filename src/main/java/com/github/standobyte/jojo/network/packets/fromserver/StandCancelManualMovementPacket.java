package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromclient.ClStandManualMovementPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandCancelManualMovementPacket {
    private final double x;
    private final double y;
    private final double z;

    public StandCancelManualMovementPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandCancelManualMovementPacket> {

        @Override
        public void encode(StandCancelManualMovementPacket msg, PacketBuffer buf) {
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
        }

        @Override
        public StandCancelManualMovementPacket decode(PacketBuffer buf) {
            return new StandCancelManualMovementPacket(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void handle(StandCancelManualMovementPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                IStandManifestation standManifestation = power.getStandManifestation();
                if (standManifestation != null && standManifestation instanceof StandEntity) {
                    StandEntity stand = (StandEntity) standManifestation;
                    stand.absMoveTo(msg.x, msg.y, msg.z);
                    PacketManager.sendToServer(new ClStandManualMovementPacket(
                            stand.getX(), stand.getY(), stand.getZ(), stand.xRot, stand.yRot, false));
                }
            });
        }

        @Override
        public Class<StandCancelManualMovementPacket> getPacketClass() {
            return StandCancelManualMovementPacket.class;
        }
    }
}
