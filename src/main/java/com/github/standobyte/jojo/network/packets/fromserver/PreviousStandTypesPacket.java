package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PreviousStandTypesPacket {
    public final boolean sendingAll;
    public final Collection<StandType<?>> allStands;
    public final StandType<?> newStand;
    
    public static PreviousStandTypesPacket allStands(Collection<StandType<?>> allStands) {
        return new PreviousStandTypesPacket(true, allStands, null);
    }

    public static PreviousStandTypesPacket newStand(StandType<?> newStand) {
        return new PreviousStandTypesPacket(false, null, newStand);
    }
    
    private PreviousStandTypesPacket(boolean sendingAll, Collection<StandType<?>> allStands, StandType<?> newStand) {
        this.sendingAll = sendingAll;
        this.allStands = allStands;
        this.newStand = newStand;
    }
    
    
    public static class Handler implements IModPacketHandler<PreviousStandTypesPacket> {

        @Override
        public void encode(PreviousStandTypesPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.sendingAll);
            if (msg.sendingAll) {
                NetworkUtil.writeCollection(buf, msg.allStands, type -> buf.writeRegistryId(type), false);
            }
            else {
                buf.writeRegistryId(msg.newStand);
            }
        }

        @Override
        public PreviousStandTypesPacket decode(PacketBuffer buf) {
            boolean sentAll = buf.readBoolean();
            if (sentAll) {
                return allStands(NetworkUtil.readCollection(buf, () -> buf.readRegistryIdSafe(StandType.class)));
            }
            else {
                return newStand(buf.readRegistryIdSafe(StandType.class));
            }
        }

        @Override
        public void handle(PreviousStandTypesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getPreviousStandsSet().handlePacket(msg);
            });
        }

        @Override
        public Class<PreviousStandTypesPacket> getPacketClass() {
            return PreviousStandTypesPacket.class;
        }
    }
}
