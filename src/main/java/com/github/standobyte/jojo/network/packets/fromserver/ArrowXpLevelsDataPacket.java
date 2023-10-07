package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandArrowHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ArrowXpLevelsDataPacket {
    public final int levels;
    public final int gotStands;
    
    public ArrowXpLevelsDataPacket(int levels, int gotStands) {
        this.levels = levels;
        this.gotStands = gotStands;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ArrowXpLevelsDataPacket> {

        @Override
        public void encode(ArrowXpLevelsDataPacket msg, PacketBuffer buf) {
            buf.writeVarInt(msg.levels);
            buf.writeVarInt(msg.gotStands);
        }

        @Override
        public ArrowXpLevelsDataPacket decode(PacketBuffer buf) {
            return new ArrowXpLevelsDataPacket(buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void handle(ArrowXpLevelsDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                StandArrowHandler handler = power.getStandArrowHandler();
                handler.setFromPacket(msg);
            });
        }

        @Override
        public Class<ArrowXpLevelsDataPacket> getPacketClass() {
            return ArrowXpLevelsDataPacket.class;
        }
    }
}
