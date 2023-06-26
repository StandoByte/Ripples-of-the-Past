package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HadPowerTypesPacket {
    private final Collection<NonStandPowerType<?>> types;
    
    public HadPowerTypesPacket(Collection<NonStandPowerType<?>> types) {
        this.types = types;
    }
    
    
    public static class Handler implements IModPacketHandler<HadPowerTypesPacket> {

        @Override
        public void encode(HadPowerTypesPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.types, type -> buf.writeRegistryId(type), false);
        }

        @Override
        public HadPowerTypesPacket decode(PacketBuffer buf) {
            return new HadPowerTypesPacket(NetworkUtil.readCollection(buf, () -> buf.readRegistryIdSafe(NonStandPowerType.class)));
        }

        @Override
        public void handle(HadPowerTypesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                msg.types.forEach(type -> {
                    power.addHadPowerBefore(type);
                });
            });
        }

        @Override
        public Class<HadPowerTypesPacket> getPacketClass() {
            return HadPowerTypesPacket.class;
        }
    }
}
